import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.resultset.ResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;

import java.util.Random;

public class TestCqEngine {
    
    private static Random randomObj = new Random(1990);
    

    public static void main(String[] args){
       Alignment m = new Alignment(false, false, false);
       m.add("http://exampleLeftWithALongURI/13245", "http://exampleRightWithALongURI/13245");
        for(int i = 0; i < 50000; i++){
            int left = randomObj.nextInt(Integer.MAX_VALUE);
            int right = randomObj.nextInt(Integer.MAX_VALUE);            
            m.add("http://exampleLeftWithALongURI/" + left, "http://exampleRightWithALongURI/" + right);
        }
        
        ResultSet<Correspondence> r = m.retrieve(QueryFactory.equal(Correspondence.SOURCE, "http://exampleLeftWithALongURI/13245"));
        System.out.println(r.getRetrievalCost());
        r.forEach(System.out::println);
        
        m.addIndex(HashIndex.onAttribute(Correspondence.SOURCE));
        try{
            m.addIndex(HashIndex.onAttribute(Correspondence.SOURCE));
        }catch(IllegalStateException e){
            System.out.println("test");
        }
        //m.addIndex(HashIndex.onAttribute(Correspondence.SOURCE));
        
        
        r = m.retrieve(QueryFactory.equal(Correspondence.SOURCE, "http://exampleLeftWithALongURI/13245"));
        System.out.println(r.getRetrievalCost());
        r.forEach(System.out::println);

    }
    
}
