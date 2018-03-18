package cyclops.companion.rx2;

import com.oath.cyclops.anym.AnyMSeq;
import com.oath.cyclops.rx2.adapter.FlowableReactiveSeq;
import cyclops.control.Either;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.monads.AnyM;
import cyclops.monads.Rx2Witness;
import cyclops.monads.Rx2Witness.flowable;
import cyclops.monads.WitnessType;
import cyclops.monads.XorM;
import cyclops.monads.transformers.StreamT;
import cyclops.reactive.ReactiveSeq;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import lombok.experimental.UtilityClass;
import org.reactivestreams.Publisher;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * Companion class for working with Reactor Flowable types
 *
 * @author johnmcclean
 *
 */
@UtilityClass
public class Flowables {


    public static  <W1 extends WitnessType<W1>,T> XorM<W1,flowable,T> xorM(Flowable<T> type){
        return XorM.right(anyM(type));
    }

    public static  <T,R> Flowable<R> tailRec(T initial, Function<? super T, ? extends Flowable<? extends Either<T, R>>> fn) {
        Flowable<Either<T, R>> next = Flowable.just(Either.left(initial));

        boolean newValue[] = {true};
        for(;;){

            next = next.flatMap(e -> e.visit(s -> {
                        newValue[0]=true;
                        return fn.apply(s); },
                    p -> {
                        newValue[0]=false;
                        return Flowable.just(e);
                    }));
            if(!newValue[0])
                break;

        }

        return next.filter(Either::isRight).map(e->e.orElse(null));
    }

    public static <T> Flowable<T> raw(AnyM<flowable,T> anyM){
        return flowable(anyM);
    }
    public static <T,W extends WitnessType<W>> AnyM<W,Flowable<T>> fromStream(AnyM<W,Stream<T>> anyM){
        return anyM.map(s->flowableFrom(ReactiveSeq.fromStream(s)));
    }
    public static <T> Flowable<T> narrow(Flowable<? extends T> observable) {
        return (Flowable<T>)observable;
    }
    public static  <T> Flowable<T> flowableFrom(ReactiveSeq<T> stream){

        return stream.visit(sync->Flowable.fromIterable(stream),
                            rs->Flowable.fromPublisher(stream),
                            async-> Observables.fromStream(stream).toFlowable(BackpressureStrategy.BUFFER));


    }

    public static <W extends WitnessType<W>,T> StreamT<W,T> flowablify(StreamT<W,T> nested){
        AnyM<W, Stream<T>> anyM = nested.unwrap();
        AnyM<W, ReactiveSeq<T>> flowableM = anyM.map(s -> {
            if (s instanceof FlowableReactiveSeq) {
                return (FlowableReactiveSeq)s;
            }
            if(s instanceof ReactiveSeq){
            return ((ReactiveSeq<T>)s).visit(sync->new FlowableReactiveSeq<T>(Flowable.fromIterable(sync)),
                            rs->new FlowableReactiveSeq<T>(Flowable.fromPublisher(rs)),
                            async ->new FlowableReactiveSeq<T>(Observables.fromStream(async).toFlowable(BackpressureStrategy.BUFFER)));
            }
             return new FlowableReactiveSeq<T>(Flowable.fromIterable(ReactiveSeq.fromStream(s)));
        });
        StreamT<W, T> res = StreamT.of(flowableM);
        return res;
    }

    public static <W extends WitnessType<W>,T,R> R nestedFlowable(StreamT<W,T> nested, Function<? super AnyM<W,Flowable<T>>,? extends R> mapper){
        return mapper.apply(nestedFlowable(nested));
    }
    public static <W extends WitnessType<W>,T> AnyM<W,Flowable<T>> nestedFlowable(StreamT<W,T> nested){
        AnyM<W, Stream<T>> anyM = nested.unwrap();
        return anyM.map(s->{
            if(s instanceof FlowableReactiveSeq){
                return ((FlowableReactiveSeq)s).getFlowable();
            }
            if(s instanceof ReactiveSeq){
                ReactiveSeq<T> r = (ReactiveSeq<T>)s;
                return r.visit(sync->Flowable.fromIterable(sync),rs->Flowable.fromPublisher((Publisher)s),
                        async->Flowable.fromPublisher(async));
            }
            if(s instanceof Publisher){
                return Flowable.<T>fromPublisher((Publisher)s);
            }
            return Flowable.fromIterable(ReactiveSeq.fromStream(s));
        });
    }

    public static <W extends WitnessType<W>,T> StreamT<W,T> liftM(AnyM<W,Flowable<T>> nested){
        AnyM<W, ReactiveSeq<T>> monad = nested.map(s -> new FlowableReactiveSeq<T>(s));
        return StreamT.of(monad);
    }

    public static <T> ReactiveSeq<T> reactiveSeq(Flowable<T> flowable){
        return new FlowableReactiveSeq<>(flowable);
    }

    public static <T> ReactiveSeq<T> reactiveSeq(Publisher<T> flowable){
        return new FlowableReactiveSeq<>(Flowable.fromPublisher(flowable));
    }

    public static ReactiveSeq<Integer> range(int start, int end){
       return reactiveSeq(Flowable.range(start,end));
    }
    public static <T> ReactiveSeq<T> of(T... data) {
        return reactiveSeq(Flowable.fromArray(data));
    }
    public static  <T> ReactiveSeq<T> of(T value){
        return reactiveSeq(Flowable.just(value));
    }

    public static <T> ReactiveSeq<T> ofNullable(T nullable){
        if(nullable==null){
            return empty();
        }
        return of(nullable);
    }



    public static <T> ReactiveSeq<T> empty() {
        return reactiveSeq(Flowable.empty());
    }


    public static <T> ReactiveSeq<T> error(Throwable error) {
        return reactiveSeq(Flowable.error(error));
    }







    public static <T> ReactiveSeq<T> from(Publisher<? extends T> source) {
       return reactiveSeq(Flowable.fromPublisher(source));
    }


    public static <T> ReactiveSeq<T> fromIterable(Iterable<? extends T> it) {
        return reactiveSeq(Flowable.fromIterable(it));
    }


    public static <T> ReactiveSeq<T> fromStream(Stream<? extends T> s) {
        return reactiveSeq(flowableFrom(ReactiveSeq.fromStream((Stream<T>)s)));
    }








    @SafeVarargs
    public static <T> ReactiveSeq<T> just(T... data) {
        return reactiveSeq(Flowable.fromArray(data));
    }


    public static <T> ReactiveSeq<T> just(T data) {
        return reactiveSeq(Flowable.just(data));
    }


    /**
     * Construct an AnyM type from a Flowable. This allows the Flowable to be manipulated according to a standard interface
     * along with a vast array of other Java Monad implementations
     *
     * <pre>
     * {@code
     *
     *    AnyMSeq<Integer> flowable = Flowables.anyM(Flowable.just(1,2,3));
     *    AnyMSeq<Integer> transformedFlowable = myGenericOperation(flowable);
     *
     *    public AnyMSeq<Integer> myGenericOperation(AnyMSeq<Integer> monad);
     * }
     * </pre>
     *
     * @param flowable To wrap inside an AnyM
     * @return AnyMSeq wrapping a flowable
     */
    public static <T> AnyMSeq<flowable,T> anyM(Flowable<T> flowable) {
        return AnyM.ofSeq(reactiveSeq(flowable), Rx2Witness.flowable.INSTANCE);
    }

    public static <T> Flowable<T> flowable(AnyM<flowable,T> flowable) {

        FlowableReactiveSeq<T> flowableSeq = flowable.unwrap();
        return flowableSeq.getFlowable();
    }

    /**
     * Perform a For Comprehension over a Flowable, accepting 3 generating functions.
     * This results in a four level nested internal iteration over the provided Publishers.
     *
     *  <pre>
      * {@code
      *
      *   import static cyclops.companion.reactor.Flowables.forEach4;
      *
          forEach4(Flowable.range(1,10),
                  a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                  (a,b) -> Maybe.<Integer>of(a+b),
                  (a,b,c) -> Mono.<Integer>just(a+b+c),
                  Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Flowable
     * @param value2 Nested publisher
     * @param value3 Nested publisher
     * @param value4 Nested publisher
     * @param yieldingFunction  Generates a result per combination
     * @return Flowable with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Flowable<R> forEach4(Flowable<? extends T1> value1,
                                                               Function<? super T1, ? extends Publisher<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, ? extends Publisher<R3>> value4,
            Function4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Flowable<R1> a = Flowable.fromPublisher(value2.apply(in));
            return a.flatMap(ina -> {
                Flowable<R2> b = Flowable.fromPublisher(value3.apply(in,ina));
                return b.flatMap(inb -> {
                    Flowable<R3> c = Flowable.fromPublisher(value4.apply(in,ina,inb));
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });


    }

    /**
     * Perform a For Comprehension over a Flowable, accepting 3 generating functions.
     * This results in a four level nested internal iteration over the provided Publishers.
     * <pre>
     * {@code
     *
     *  import static cyclops.companion.reactor.Flowables.forEach4;
     *
     *  forEach4(Flowable.range(1,10),
                            a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                            (a,b) -> Maybe.<Integer>just(a+b),
                            (a,b,c) -> Mono.<Integer>just(a+b+c),
                            (a,b,c,d) -> a+b+c+d <100,
                            Tuple::tuple);
     *
     * }
     * </pre>
     *
     * @param value1 top level Flowable
     * @param value2 Nested publisher
     * @param value3 Nested publisher
     * @param value4 Nested publisher
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return Flowable with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Flowable<R> forEach4(Flowable<? extends T1> value1,
            Function<? super T1, ? extends Publisher<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, ? extends Publisher<R3>> value4,
            Function4<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Function4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Flowable<R1> a = Flowable.fromPublisher(value2.apply(in));
            return a.flatMap(ina -> {
                Flowable<R2> b = Flowable.fromPublisher(value3.apply(in,ina));
                return b.flatMap(inb -> {
                    Flowable<R3> c = Flowable.fromPublisher(value4.apply(in,ina,inb));
                    return c.filter(in2->filterFunction.apply(in,ina,inb,in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }

    /**
     * Perform a For Comprehension over a Flowable, accepting 2 generating functions.
     * This results in a three level nested internal iteration over the provided Publishers.
     *
     * <pre>
     * {@code
     *
     * import static cyclops.companion.reactor.Flowables.forEach;
     *
     * forEach(Flowable.range(1,10),
                            a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                            (a,b) -> Maybe.<Integer>of(a+b),
                            Tuple::tuple);
     *
     * }
     * </pre>
     *
     *
     * @param value1 top level Flowable
     * @param value2 Nested publisher
     * @param value3 Nested publisher
     * @param yieldingFunction Generates a result per combination
     * @return Flowable with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Flowable<R> forEach3(Flowable<? extends T1> value1,
            Function<? super T1, ? extends Publisher<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Flowable<R1> a = Flowable.fromPublisher(value2.apply(in));
            return a.flatMap(ina -> {
                Flowable<R2> b = Flowable.fromPublisher(value3.apply(in, ina));
                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
            });


        });

    }
        /**
         * Perform a For Comprehension over a Flowable, accepting 2 generating functions.
         * This results in a three level nested internal iteration over the provided Publishers.
         * <pre>
         * {@code
         *
         * import static cyclops.companion.reactor.Flowables.forEach;
         *
         * forEach(Flowable.range(1,10),
                       a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                       (a,b) -> Maybe.<Integer>of(a+b),
                       (a,b,c) ->a+b+c<10,
                       Tuple::tuple).toListX();
         * }
         * </pre>
         *
         * @param value1 top level Flowable
         * @param value2 Nested publisher
         * @param value3 Nested publisher
         * @param filterFunction A filtering function, keeps values where the predicate holds
         * @param yieldingFunction Generates a result per combination
         * @return
         */
    public static <T1, T2, R1, R2, R> Flowable<R> forEach3(Flowable<? extends T1> value1,
            Function<? super T1, ? extends Publisher<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
            Function3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Flowable<R1> a = Flowable.fromPublisher(value2.apply(in));
            return a.flatMap(ina -> {
                Flowable<R2> b = Flowable.fromPublisher(value3.apply(in,ina));
                return b.filter(in2->filterFunction.apply(in,ina,in2))
                        .map(in2 -> yieldingFunction.apply(in, ina, in2));
            });



        });

    }

    /**
     * Perform a For Comprehension over a Flowable, accepting an additonal generating function.
     * This results in a two level nested internal iteration over the provided Publishers.
     *
     * <pre>
     * {@code
     *
     *  import static cyclops.companion.reactor.Flowables.forEach;
     *  forEach(Flowable.range(1, 10), i -> Flowable.range(i, 10), Tuple::tuple)
              .subscribe(System.out::println);

       //(1, 1)
         (1, 2)
         (1, 3)
         (1, 4)
         ...
     *
     * }</pre>
     *
     * @param value1 top level Flowable
     * @param value2 Nested publisher
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Flowable<R> forEach(Flowable<? extends T> value1, Function<? super T, Flowable<R1>> value2,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Flowable<R1> a = Flowable.fromPublisher(value2.apply(in));
            return a.map(in2 -> yieldingFunction.apply(in,  in2));
        });

    }

    /**
     *
     * <pre>
     * {@code
     *
     *   import static cyclops.companion.reactor.Flowables.forEach;
     *
     *   forEach(Flowable.range(1, 10), i -> Flowable.range(i, 10),(a,b) -> a>2 && b<10,Tuple::tuple)
               .subscribe(System.out::println);

       //(3, 3)
         (3, 4)
         (3, 5)
         (3, 6)
         (3, 7)
         (3, 8)
         (3, 9)
         ...

     *
     * }</pre>
     *
     *
     * @param value1 top level Flowable
     * @param value2 Nested publisher
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Flowable<R> forEach(Flowable<? extends T> value1,
            Function<? super T, ? extends Publisher<R1>> value2,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Flowable<R1> a = Flowable.fromPublisher(value2.apply(in));
            return a.filter(in2->filterFunction.apply(in,in2))
                    .map(in2 -> yieldingFunction.apply(in,  in2));
        });

    }



}