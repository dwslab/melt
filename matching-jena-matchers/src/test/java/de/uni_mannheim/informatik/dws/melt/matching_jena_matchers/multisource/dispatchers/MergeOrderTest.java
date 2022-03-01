package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MergeOrderTest {
    
    @Test
    public void testGetHeight(){
        
        for(int i = 1; i < 10; i++){
            int n = (int)Math.pow(2, i);
            int[][] tree = getBalanced(n);
            MergeOrder m = new MergeOrder(tree);
            int height = m.getHeight();
            assertEquals(i + 1, height);
        }
        
        for(int i = 2; i < 10; i++){
            for(boolean variant : Arrays.asList(true, false)){
                int[][] tree = getLeftSkewed(i, variant);
                MergeOrder m = new MergeOrder(tree);
                int height = m.getHeight();
                int expected = i;
                assertEquals(expected, height);
            }
        }
    }
    
    @Test
    public void testGetHeightLargeValues(){

        int n = (int)Math.pow(2, 19);
        int[][] tree = getBalanced(n);
        int height = new MergeOrder(tree).getHeight();
        assertEquals(20, height);

        for(boolean variant : Arrays.asList(true, false)){
            int[][] treeTwo = getLeftSkewed(450_000, variant);
            int heightTwo = new MergeOrder(treeTwo).getHeight();
            int expected = 450_000;
            assertEquals(expected, heightTwo);
        }
        
    }
    
    @Test
    public void testGetCountOfParallelExecutions(){        
        assertIterableEquals(Arrays.asList(1,1,1,1), new MergeOrder(getRightSkewed(5, true)).getCountOfParallelExecutions());
        assertIterableEquals(Arrays.asList(4,2,1), new MergeOrder(getBalanced(8)).getCountOfParallelExecutions());
        assertIterableEquals(Arrays.asList(8,4,2,1), new MergeOrder(getBalanced(16)).getCountOfParallelExecutions());
    }
    
    
    
    //public helper methods:
    
    public static int[][] getRightSkewed(int count, boolean isVersionA){
        int[][] array = new int[count-1][2];
        array[0][0] = isVersionA ? count - 2 : count - 1;
        array[0][1] = isVersionA ? count - 1 : count - 2;
        for(int i = 1; i < count - 1; i++){
            int x = count + i - 1;
            int y = count - i - 2;
            array[i][0] = isVersionA ? x : y;
            array[i][1] = isVersionA ? y : x;
        }
        return array;
    }
    
    public static int[][] getLeftSkewed(int count, boolean isVersionA){
        if(count <= 1 )
            throw new IllegalArgumentException("Count must be greater or equal to two. But it was " + count);
        int[][] array = new int[count-1][2];
        array[0][0] = isVersionA ? 0 : 1;
        array[0][1] = isVersionA ? 1 : 0;
        for(int i = 1; i < count - 1; i++){
            int x = count + i - 1;
            int y = i + 1;
            array[i][0] = isVersionA ? x : y;
            array[i][1] = isVersionA ? y : x;
        }
        return array;
    }
    
    public static int[][] getBalanced(int count){
        if(isPowerOfTwo(count) == false){
            throw new IllegalArgumentException("This function only works for values which are a power of two.");
        }
        int[][] array = new int[count-1][2];
        for(int i =0; i <  count-1; i++){
            array[i][0] = (i * 2);
            array[i][1] = (i * 2) + 1;
        }
        return array;
    }
    
    private static boolean isPowerOfTwo(int x)
    {
        /* First x in the below expression is
        for the case when x is 0 */
        return x != 0 && ((x & (x - 1)) == 0);
    }
    
    
    
    /*
    public static void main(String[] args){
        MergeTreeUtil.logCountOfParallelExecutions(getRightSkewed(5, true));
        MergeTreeUtil.logCountOfParallelExecutions(getBalanced(8));
        System.out.println(Arrays.deepToString(getRightSkewed(5, true)));
        System.out.println(Arrays.deepToString(getRightSkewed(12, true)));
    }
    */
}
