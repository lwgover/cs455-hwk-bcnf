import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NormalizerTest {

    @org.junit.jupiter.api.Test
    void BCNFDecompose() {
        // Employee(ssn, name, cartID, title, wage)
        Set<String> employee = new HashSet<>(Arrays.asList("ssn", "name", "cartID", "title", "wage"));
        FD e1 = new FD(List.of("ssn"), List.of("name")); // ssn --> name
        FD e2 = new FD(Arrays.asList("ssn", "cartID"), Arrays.asList("title", "wage")); // ssn,cartID --> title,wage
        FDSet empFDs = new FDSet(e1, e2);
        assertEquals(Set.of(Set.of("cartID", "title", "ssn", "wage"), Set.of("name", "ssn")), Normalizer.BCNFDecompose(employee, empFDs));
    }

    @org.junit.jupiter.api.Test
    void BCNFDecompose2() {
        Set<String> S = new HashSet<>(Arrays.asList("A", "B", "C", "D")); // Relation S(A,B,C,D)
        FD s1 = new FD(List.of("A"), List.of("B")); // A --> B
        FD s2 = new FD(List.of("B"), List.of("C")); // B --> C
        FDSet fdsetS = new FDSet(s1, s2);
        assertEquals(Set.of(Set.of("A", "B"), Set.of("A", "C"), Set.of("A", "D")), Normalizer.BCNFDecompose(S, fdsetS));
    }

    @org.junit.jupiter.api.Test
    void BCNFDecomposeImmutableTest() {
        FD s1 = new FD(List.of("A"), List.of("B")); // A --> B
        FD s2 = new FD(List.of("B"), List.of("C")); // B --> C
        FDSet fdsetS = new FDSet(s1, s2);
        assertTrue(fdsetS.getSet().contains(new FD(List.of("A"), List.of("B"))));
        assertTrue(fdsetS.getSet().contains(new FD(List.of("B"), List.of("C"))));
        assertEquals(2, fdsetS.size());
    }
    @org.junit.jupiter.api.Test
    void BCNFDecopose3() {
        Set<String> U = new HashSet<>(Arrays.asList("A", "B", "C", "D", "E"));  // Relation U(A,B,C,D,E)
        FD f1 = new FD(Arrays.asList("A", "E"), List.of("D")); // AE --> D
        FD f2 = new FD(Arrays.asList("A", "B"), List.of("C")); // AB --> C
        FD f3 = new FD(List.of("D"), List.of("B")); // D --> B
        FDSet fdsetU = new FDSet(f1, f2, f3);
        assertEquals(Set.of(Set.of("B", "D"), Set.of("A", "C", "D"), Set.of("A", "D", "E")), Normalizer.BCNFDecompose(U, fdsetU));
    }

    @org.junit.jupiter.api.Test
    void isBCNF() {
        FD f1 = new FD(List.of("ssn"), List.of("name")); // ssn --> name
        FD f2 = new FD(Arrays.asList("ssn", "name"), List.of("eyecolor")); // ssn,name --> eyecolor
        FDSet fdset = new FDSet(f1, f2);

        Set<String> people = new HashSet<>(Arrays.asList("ssn", "name", "eyecolor")); // Relation people(ssn,name,eyecolor)
        assertTrue(Normalizer.isBCNF(people, fdset));
    }

    @org.junit.jupiter.api.Test
    void isBCNFImmutableTest() {
        FD f1 = new FD(List.of("ssn"), List.of("name")); // ssn --> name
        FD f2 = new FD(Arrays.asList("ssn", "name"), List.of("eyecolor")); // ssn,name --> eyecolor
        FDSet fdset = new FDSet(f1, f2);

        assertTrue(fdset.getSet().contains(new FD(List.of("ssn"), List.of("name"))));
        assertTrue(fdset.getSet().contains(new FD(Arrays.asList("ssn", "name"), List.of("eyecolor"))));
        assertEquals(2, fdset.size());
    }

    @org.junit.jupiter.api.Test
    void isBCNFtheSqueakquel() { // like alvin and the chipmunks 2: the squeakquel
        FD f1 = new FD(List.of("ssn"), List.of("name")); // ssn --> name
        FD f2 = new FD(Arrays.asList("ssn", "name"), List.of("eyecolor")); // ssn,name --> eyecolor
        FD f3 = new FD(List.of("name"), List.of("eyecolor")); // name --> eyecolor (violates BCNF)
        FDSet fdset = new FDSet(f1, f2, f3);

        Set<String> people = new HashSet<>(Arrays.asList("ssn", "name", "eyecolor")); // Relation people(ssn,name,eyecolor)
        assertFalse(Normalizer.isBCNF(people, fdset));
    }

    @org.junit.jupiter.api.Test
    void ERRORisBCNF() {
        FD f1 = new FD(List.of("ssn"), List.of("name")); // ssn --> name
        FD f2 = new FD(List.of("ssn"), List.of("eyecolor")); // ssn --> eyecolor
        FDSet fdset = new FDSet(f1, f2);

        Set<String> people = new HashSet<>(Arrays.asList("ssn", "name")); // Relation people(ssn, name)
        assertThrows(java.lang.IllegalArgumentException.class, () -> Normalizer.findSuperkeys(people, fdset));
    }

    @org.junit.jupiter.api.Test
    void findSuperkeys() {
        Set<String> test = new HashSet<>(Arrays.asList("A", "D", "E"));
        FD f1test = new FD(Arrays.asList("A", "E"), List.of("D")); // AE --> D

        assertEquals(Set.of(Set.of("A", "E"), Set.of("A", "E", "D")), Normalizer.findSuperkeys(test, new FDSet(f1test)));

        //#########################     SECOND TEST     #########################

        FD f1 = new FD(List.of("ssn"), List.of("name")); // ssn --> name
        FD f2 = new FD(List.of("ssn"), List.of("eyecolor")); // ssn --> eyecolor
        FDSet fdset = new FDSet(f1, f2);

        Set<String> people = new HashSet<>(Arrays.asList("ssn", "name", "eyecolor")); // Relation people(ssn, name, eyecolor)
        assertEquals(Set.of(Set.of("ssn"), Set.of("ssn", "eyecolor"), Set.of("name", "ssn"), Set.of("name", "ssn", "eyecolor")), Normalizer.findSuperkeys(people, fdset));

    }
}