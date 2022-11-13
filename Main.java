import java.util.*;

public class Main {
  public static void main(String[] args) {
    Set<String> test = new HashSet<>(Arrays.asList("A", "D", "E"));
    FD f1test = new FD(Arrays.asList("A", "E"), Arrays.asList("D")); // AE --> D
    System.out.println(Normalizer.findSuperkeys(test,new FDSet(f1test)));


    // U(A,B,C,D,E)
    Set<String> U = new HashSet<>(Arrays.asList("A", "B", "C", "D", "E"));
    FD f1 = new FD(Arrays.asList("A", "E"), Arrays.asList("D")); // AE --> D
    FD f2 = new FD(Arrays.asList("A", "B"), Arrays.asList("C")); // AB --> C
    FD f3 = new FD(Arrays.asList("D"), Arrays.asList("B")); // D --> B
    FDSet fdsetU = new FDSet(f1, f2, f3);
    System.out.println("Final BCNF Schemas: " + Normalizer.BCNFDecompose(U, fdsetU));

    // R(A,B,C)
    Set<String> R = new HashSet<>(Arrays.asList("A", "B", "C"));
    FD g1 = new FD(Arrays.asList("A"), Arrays.asList("B", "C")); // A --> BC
    FD g2 = new FD(Arrays.asList("B"), Arrays.asList("C")); // B --> C
    FD g3 = new FD(Arrays.asList("A"), Arrays.asList("B")); // A --> B
    FD g4 = new FD(Arrays.asList("A", "B"), Arrays.asList("C")); // AB --> C
    FDSet fdsetR = new FDSet(g1, g2, g3, g4);
    System.out.println("Final BCNF Schemas: " + Normalizer.BCNFDecompose(R, fdsetR));
  }
}
