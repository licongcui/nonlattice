package nl.util;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class SetUtil {
	
	public static <T> Set<T> union(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new TreeSet<T>(setA);
		tmp.addAll(setB);
		return tmp;
	}
	
	public static <T> Set<T> unionMultiple(Set<Set<T>> setM) {
		Set<T> tmp = new HashSet<T>();
		for (Set<T> x : setM){
			tmp.addAll(x);
		}
		return tmp;
	}
	
	public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new HashSet<T>();
		for (T x : setA)
			if (setB.contains(x))
				tmp.add(x);
		return tmp;
	}
	
	public static <T> Set<T> intersectionMultiple(Set<Set<T>> setM) {
		Set<T> tmp = new HashSet<T>();
		int i = 1;
		for (Set<T> x : setM){
			if (i == 1){
				tmp = x;
			}else{
				tmp = intersection(tmp, x);
			}
			i++;
		}
		return tmp;
	}

	public static <T> Set<T> difference(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new HashSet<T>(setA);
		tmp.removeAll(setB);
		return tmp;
	}
	
	public static String concatenateStringSet(Set<String> values){
		StringBuilder builder = new StringBuilder();
		for (String val : values) {
			builder.append(val.toString());
			builder.append(" ");
		}
		builder.setLength(builder.length() - 1);
		return builder.toString();
	}
	
	public static void main(String[] args){    
		Set<Integer> s1 = new HashSet<Integer>(){{add(1);add(2);add(3);}};
		Set<Integer> s2 = new HashSet<Integer>(){{add(1);add(2);add(3);add(4);add(5);}};
		Set<Integer> s3 = new HashSet<Integer>(){{add(3);add(4);add(5);add(6);}};
		Set<Integer> s4 = new HashSet<Integer>(){{add(6);add(7);add(8);}};
		Set<Set<Integer>> s = new HashSet<Set<Integer>>();
		s.add(s1);
		s.add(s2);
		s.add(s3);
		s.add(s4);
		System.out.println(intersectionMultiple(s));
		System.out.println(unionMultiple(s));

	}
}
