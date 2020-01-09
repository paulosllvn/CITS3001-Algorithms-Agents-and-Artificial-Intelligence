import java.util.Arrays;
import java.util.Comparator;

public class KnapsackIMP implements Knapsack {

    public static void main(String[] args) {
        int[] values = {22,40,28,29,36,48,28,36,8,2,40,4,45,19,15,46,32,47,46,9,15,25};
        int[] weights = {2,45,12,33,6,18,26,15,17,44,31,21,15,48,38,23,35,37,34,22,};
        int capacity = 136;

        double maxValueFractional = fractionalKnapsack(weights, values, capacity);
        System.out.println("Maximum value we can obtain from fractional is = " +
                maxValueFractional);

        int maxValueDiscrete = discreteKnapsack(weights, values, capacity);
        System.out.println("Maximum value we can obtain from discrete is = " +
                maxValueDiscrete);
    }




    public static int fractionalKnapsack(int[] weights, int[] values, int capacity){

        ObjectValue[] objVal = new ObjectValue[weights.length];

        for(int i = 0; i <weights.length; i++) {
            objVal[i] = new ObjectValue(weights[i], values[i]);
        }




        //Arrays.sort(objVal, Comparator.comparing(ObjectValue::getCost));

        Arrays.sort(objVal, new Comparator<ObjectValue>() {
            public int compare(ObjectValue a, ObjectValue b) {

                if(a.getCost() < b.getCost())
                {
                    return -1;
                }
                else if(a.getCost() > b.getCost())
                {
                    return 1;
                }
                return 0;

            }

        });

        for(int i = 0; i<objVal.length;i++){
            System.out.println(objVal[i].getCost());
        }


        double totalvalue = 0;
        double totalweight = 0;
        double fraction = 0.0;
        System.out.println("currentvalue" + " "+ "currentweight" + " "+ "totalweight" +  " "+ "totalvalue" + " "+ "capacity"+ " "+"fraction");

        for(int k = objVal.length-1; k>=0; k--) {
            int currentweight = objVal[k].getWeight();
            int currentvalue = objVal[k].getValue();

            System.out.println(currentvalue + "                 "+ currentweight + "         "+ totalweight +  "            "+ totalvalue + "      "+ capacity+"            "+ fraction);


            if(currentweight <= capacity){
                totalvalue += currentvalue;
                totalweight += currentweight;
                capacity = capacity - currentweight;
            }
            else{
                fraction = (double)capacity/currentweight;
                totalvalue += fraction * currentvalue;
                capacity = (int) (capacity - (currentweight * fraction));
                System.out.println(currentvalue + "                 "+ currentweight + "         "+ totalweight +  "            "+ totalvalue + "      "+ capacity+"            "+ fraction);
            }


        }


        return (int)totalvalue;

    }

    public static int max(int a, int b) {

        if (a > b) {
            return a;
        } else {
            return b;
        }
    }


    public static int discreteKnapsack(int[] weights, int[] values, int capacity) {

        int W = weights.length;

        int[][] matrix = new int[W+1][capacity+1];

        for(int i = 0; i<=W;i++){

            for(int j = 0; j<=capacity;j++){

                if(i==0 || j==0){
                    matrix[i][j] = 0;
                }
                else if(weights[i-1]<=j){
                    matrix[i][j] = max(matrix[i-1][j],matrix[i-1][j-weights[i-1]]+values[i-1]);
                }
                else{
                    matrix[i][j] = matrix[i-1][j];
                }

            }
        }
        return matrix[W][capacity];
    }



    
static class ObjectValue {
    int weight, value;

    public ObjectValue(int weights, int values) {
        this.weight = weights;
        this.value = values;
        }

    public double getCost(){


        return (double)this.value/(double)this.weight;

    }
    public int getWeight(){ return this.weight;
    }

    public int getValue(){
        return this.value;
    }

    }




}





