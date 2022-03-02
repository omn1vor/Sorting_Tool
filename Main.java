package sorting;

public class Main {

    public static void main(final String[] args) {
        try {
            SortingTool<?> sortingTool = SortingTool.getSorter(args);
            sortingTool.readData();
            sortingTool.printSort();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

    }

}


