package taskone;

import java.util.List;
import java.util.ArrayList;

class StringList {
    
    List<String> strings = new ArrayList<String>();

    public void add(String str) {
        int pos = strings.indexOf(str);
        if (pos < 0) {
            strings.add(str);
        }
    }
    
    public String pop() {
    	int size = strings.size();
    	String popped;
    	if(size > 0) {
    		popped = strings.remove(size - 1);
    	}else {
    		popped = "null";
    	}
    	return popped;
    }
    
    public String Switch(int idx1, int idx2) {
    	int size = strings.size();
    	String string1, string2;
    	if(idx1 <= (size - 1) && idx2 <= (size -1) && idx1 >= 0 && idx2 >=0) {
    		
    		if(idx1 < idx2) {
    			string1 = strings.remove(idx1);
    			string2 = strings.remove(idx2 - 1);
    			strings.add(idx1, string2);
        		strings.add(idx2, string1);
    		}else if(idx1 == idx2) {
    			return strings.toString();
    		}else {
    			string1 = strings.remove(idx1);
    			string2 = strings.remove(idx2);
        		strings.add(idx2, string1);
        		strings.add(idx1, string2);
    		}
    		
    		return strings.toString();
    	}else {
    		return "null";
    	}
    }

    public boolean contains(String str) {
        return strings.indexOf(str) >= 0;
    }

    public int size() {
        return strings.size();
    }

    public String toString() {
        return strings.toString();
    }
}