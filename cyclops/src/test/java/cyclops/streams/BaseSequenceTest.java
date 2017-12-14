package cyclops.streams;

import static cyclops.reactive.ReactiveSeq.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import cyclops.collections.mutable.ListX;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.junit.Before;
import org.junit.Test;

import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;


public  class BaseSequenceTest {
    public static Executor ex =  Executors.newFixedThreadPool(10);


	ReactiveSeq<Integer> empty;
	ReactiveSeq<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
	}




	protected Object value() {

		return "jello";
	}
	private int value2() {

		return 200;
	}


	@Test
	public void batchBySize(){
		System.out.println(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).size(),is(2));
	}





	@Test
	public void limitWhileTest(){

		List<Integer> list = new ArrayList<>();
		while(list.size()==0){
			list = of(1,2,3,4,5,6).limitWhile(it -> it<4)
						.peek(it -> System.out.println(it)).collect(Collectors.toList());

		}
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(list.get(0)));




	}

    @Test
    public void testScanLeftStringConcat() {
        assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
        		is(4));
    }
    @Test
    public void testScanLeftSum() {
    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanLeft(0, (u, t) -> u + t).toList().size(),
    			is(asList(0, 1, 3, 6).size()));
    }
    @Test
    public void testScanRightStringConcatMonoid() {
        assertThat(of("a", "b", "c").scanRight(Monoid.fromBiFunction("", String::concat)).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
    }
    @Test
    public void testScanRightStringConcat() {
        assertThat(of("a", "b", "c").scanRight("", String::concat).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
    }
    @Test
    public void testScanRightSum() {
    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanRight(0, (t, u) -> u + t).toList().size(),
            is(asList(0, 3, 5, 6).size()));


    }



    @Test
    public void testReverse() {

        assertThat( of(1, 2, 3).reverse().toList(), equalTo(asList(3, 2, 1)));
    }
    @Test
    public void testReverseList() {

        assertThat( ReactiveSeq.fromList(Arrays.asList(10,400,2,-1))
        				.reverse().toList(), equalTo(asList(-1, 2, 400,10)));
    }
    @Test
    public void testReverseListLimit() {

        assertThat( ReactiveSeq.fromList(Arrays.asList(10,400,2,-1)).limit(2)
        				.reverse().toList(), equalTo(asList(400, 10)));
    }
	@Test
	public void testReverseListLimitOneShot() {

		assertThat( ReactiveSeq.oneShotList(Arrays.asList(10,400,2,-1)).limit(2)
				.reverse().toList(), equalTo(asList(-1, 2)));
	}
    @Test
    public void testReverseRange() {

        assertThat( ReactiveSeq.range(0,10)
        				.reverse().toList(), equalTo(asList(10,9,8,7,6,5,4,3,2,1)));
    }


    @Test
    public void testCycle() {

    	   assertEquals(asList(1, 1, 1, 1, 1,1),of(1).cycle().limit(6).toList());

    }

    @Test
    public void testIterable() {
        List<Integer> list = of(1, 2, 3).to().collection(LinkedList::new);

        for (Integer i :of(1, 2, 3)) {
            assertThat(list,hasItem(i));
        }
    }

	@Test
	public void testDuplicate(){
		 Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies._1().anyMatch(i->i==2));
		 assertTrue(copies._2().anyMatch(i->i==2));
	}
	@Test
	public void testTriplicate(){
		 Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies._1().anyMatch(i->i==2));
		 assertTrue(copies._2().anyMatch(i->i==2));
		 assertTrue(copies._3().anyMatch(i->i==2));
	}

	@Test
	public void testQuadriplicate(){
		 Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies._1().anyMatch(i->i==2));
		 assertTrue(copies._2().anyMatch(i->i==2));
		 assertTrue(copies._3().anyMatch(i->i==2));
		 assertTrue(copies._4().anyMatch(i->i==2));
	}

	@Test
	public void testDuplicateFilter(){
		 Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies._1().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._2().filter(i->i%2==0).toList().size()==3);
	}
	@Test
	public void testTriplicateFilter(){
		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies._1().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._2().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._3().filter(i->i%2==0).toList().size()==3);
	}
	@Test
	public void testQuadriplicateFilter(){
		 Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies._1().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._2().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._3().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._4().filter(i->i%2==0).toList().size()==3);
	}
	@Test
	public void testDuplicateLimit(){
		 Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 System.out.println(copies._1().limit(3).toList());
		System.out.println(copies._2().limit(3).toList());
		 assertTrue(copies._1().limit(3).toList().size()==3);
		 assertTrue(copies._2().limit(3).toList().size()==3);
	}
	@Test
	public void testTriplicateLimit(){
		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies._1().limit(3).toList().size()==3);
		 assertTrue(copies._2().limit(3).toList().size()==3);
		 assertTrue(copies._3().limit(3).toList().size()==3);
	}
	@Test
	public void testQuadriplicateLimit(){
		 Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies._1().limit(3).toList().size()==3);
		 assertTrue(copies._2().limit(3).toList().size()==3);
		 assertTrue(copies._3().limit(3).toList().size()==3);
		 assertTrue(copies._4().limit(3).toList().size()==3);
	}


	    @Test
	    public void testGroupByEager() {
	        Map<Integer, ListX<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);

	        assertThat(map1.get(0),hasItem(2));
	        assertThat(map1.get(0),hasItem(4));
	        assertThat(map1.get(1),hasItem(1));
	        assertThat(map1.get(1),hasItem(3));

	        assertEquals(2, map1.size());


	    }


	    @Test
	    public void testJoin() {
	        assertEquals("123".length(),of(1, 2, 3).join().length());
	        assertEquals("1, 2, 3".length(), of(1, 2, 3).join(", ").length());
	        assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());


	    }






	    @Test
	    public void testSkipWhile() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertTrue(s.get().skipWhile(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));

	        assertEquals(asList(), s.get().skipWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().skipUntil(i -> false).toList());
	        assertTrue(s.get().skipUntil(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
		  }

	    @Test
	    public void testSkipUntilWithNulls() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, null, 3, 4, 5);

	        assertTrue(s.get().skipUntil(i -> true).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

	    @Test
	    public void testLimitWhile() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().limitWhile(i -> false).toList());
	        assertTrue( s.get().limitWhile(i -> i < 3).toList().size()!=5);
	        assertTrue(s.get().limitWhile(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
	    }

	    @Test
	    public void testLimitUntil() {


	        assertTrue(of(1, 2, 3, 4, 5).limitUntil(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	        assertFalse(of(1, 2, 3, 4, 5).limitUntil(i -> i % 3 == 0).toList().size()==5);

	        assertEquals(asList(), of(1, 2, 3, 4, 5).limitUntil(i -> true).toList());
	    }

	    @Test
	    public void testLimitUntilWithNulls() {

	    	System.out.println(of(1, 2, null, 3, 4, 5).limitUntil(i -> false).toList());
	        assertTrue(of(1, 2, null, 3, 4, 5).limitUntil(i -> false).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }



	    @Test
	    public void testMinByMaxBy() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).orElse(-1));
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).orElse(-1));

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).orElse(-1));
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).orElse(-1));
	    }

	    @Test
	    public void testFoldLeft() {
	    	for(int i=0;i<100;i++){
		        Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().reduce("", String::concat).contains("a"));
		        assertTrue(s.get().reduce("", String::concat).contains("b"));
		        assertTrue(s.get().reduce("", String::concat).contains("c"));

		        assertEquals(3, (int) s.get().map(str->str.length()).foldLeft(0, (u, t) -> u + t));


		        assertEquals(3, (int) s.get().map(str->str.length()).foldRight(0, (t, u) -> u + t));
	    	}
	    }

	    @Test
	    public void testFoldRight(){
	    	 	Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().foldRight("", String::concat).contains("a"));
		        assertTrue(s.get().foldRight("", String::concat).contains("b"));
		        assertTrue(s.get().foldRight("", String::concat).contains("c"));
		        assertEquals(3, (int) s.get().map(str->str.length())
		        					.foldRight(0, (t, u) -> u + t));
	    }



	    @Test
		public void flattenEmpty() throws Exception {
				assertTrue(ReactiveSeq.<ReactiveSeq<Integer>>of().to(ReactiveSeq::flatten).toList().isEmpty());
		}


		@Test
		public void flattenEmptyStream() throws Exception {

			assertThat(ReactiveSeq.<Integer>of(1,2,3,4,5,5,6,8,9,10)
							.limit(10).collect(Collectors.toList()).size(),
											equalTo(asList(2, 3, 4, 5, 6, 7, 0, 0, 0, 0).size()));
		}



}
