import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides static methods for performing normalization
 *
 * @author Lucas Gover
 * @version 11 / 11 / 2022
 */
public class Normalizer {

  /**
   * Performs BCNF decomposition
   *
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return a set of relations (as attribute sets) that are in BCNF
   */
  public static Set<Set<String>> BCNFDecompose(Set<String> rel, FDSet fdset) {
    if(isBCNF(rel,fdset)){
      return Set.of(rel);
    }
    Set<Set<String>> superkeys = findSuperkeys(rel, fdset);

    FD violatingFD = null;
    for(FD fd : fdset) {
      if (rel.containsAll(fd.getLeft()) && rel.containsAll(fd.getRight()) && !fd.isTrivial() && !superkeys.contains(fd.getLeft())) {     //Identify a violating FD
        violatingFD = fd;
        break;
      }
    }
    Set<String> r1 = splitLeft(violatingFD); //finding the sets of attributes for new relations
    Set<String> r2 = splitRight(violatingFD, rel);

    FDSet closure = FDUtil.fdSetClosure(fdset);
    FDSet fds1 = new FDSet();
    FDSet fds2 = new FDSet();

    for(FD fd:closure){
      if(r1.containsAll(fd.getLeft()) && r1.containsAll(fd.getRight())){
        fds1.add(fd);
      }
      if(r2.containsAll(fd.getLeft()) && r2.containsAll(fd.getRight())){
        fds2.add(fd);
      }
    }
    Set<Set<String>> result = new HashSet<>(BCNFDecompose(r1, fds1));
    result.addAll(BCNFDecompose(r2, fds2));

    return result;

  }
  private static Set<String> splitLeft(FD fd){
    Set<String> left = new HashSet<>(fd.getRight());
    left.addAll(fd.getLeft());
    return left;
  }
  private static Set<String> splitRight(FD fd, Set<String> rel){
    Set<String> left = new HashSet<>(rel);
    left.removeAll(fd.getRight());
    left.addAll(fd.getLeft());
    return left;
  }

  /**
   * Tests whether the given relation is in BCNF. A relation is in BCNF iff the
   * left-hand attribute set of all nontrivial FDs is a super key.
   *
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return true if the relation is in BCNF with respect to the specified FD set
   */
  public static boolean isBCNF(Set<String> rel, FDSet fdset) {
    Set<Set<String>> superKeys = findSuperkeys(rel,fdset);
    for(FD fd: fdset){
      if(rel.containsAll(fd.getLeft()) && rel.containsAll(fd.getRight()) && !isTrivial(fd) && !superKeys.contains(fd.getLeft())){
        return false;
      }
    }
    return true;
  }

  private static boolean isTrivial(FD fd){
    return fd.getLeft().containsAll(fd.getRight());
  }

  /**
   * This method returns a set of super keys
   *
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return a set of super keys
   */
  public static Set<Set<String>> findSuperkeys(Set<String> rel, FDSet fdset) {
    //sanity check: are all the attributes in the FD set even in the
    // relation? Throw an IllegalArgumentException if not.
    if(!isValidRelationForFDs(rel,fdset)){throw new IllegalArgumentException();}

    // iterate through each subset of the relation's attributes, and test the attribute closure of each subset
    Set<Set<String>> superkeys = new HashSet<Set<String>>();
    for(Set<String> subset: FDUtil.powerSet(rel)){
      if(isSuperkey(subset,rel,fdset)){
        superkeys.add(subset);
      }
    }
    return superkeys;
  }

  private static boolean isSuperkey(Set<String> subset, Set<String> rel,FDSet fdset) {
    return attributeClosure(subset, fdset).equals(rel);
  }

  private static Set<String> attributeClosure(Set<String> subset, FDSet fdset) {
    Set<String> closure = new HashSet<>(subset);
    int initSize;
    do{
      initSize = closure.size();
      for(FD fd : fdset){
        if(closure.containsAll(fd.getLeft())){
          closure.addAll(fd.getRight());
        }
      }
    } while(closure.size() > initSize); //repeat until the closure doesn't grow
    return closure;
  }
  private static boolean isValidRelationForFDs(Set<String> rel, FDSet fdset){

    HashSet<String> allAttrs = new HashSet<String>();
    for(FD fd : fdset){
      allAttrs.addAll(fd.getLeft());
      allAttrs.addAll(fd.getRight());
    }
    return rel.containsAll(allAttrs);
  }

  public static boolean is2NF(Set<String> rel, FDSet fdset){
    //     For all α→β∈F:
    // •  If  is non-prime (not part of any candidate key), then α⊄CandidateKey
    // • (That is, no non-prime attribute is functionally determined by a partial key)
    Set<Set<String>> candidates = getCandidateKeys(rel,fdset);
    for(FD fd : fdset){
      for(Set<String> candidate:candidates) {
        if (Collections.disjoint(fd.getRight(), candidate) && candidate.containsAll(fd.getLeft()) && !candidate.equals(fd.getLeft())) {
          return false;
        }
      }
    }
    return true;
  }
  private static Set<Set<String>> getCandidateKeys(Set<String> rel, FDSet fdset){
    Set<Set<String>> superkeys = findSuperkeys(rel,fdset);
    Set<Set<String>> candidateKeys = new HashSet<>();
    for(Set<String> keys:superkeys){
      if(candidateKeys.size() <= 0){
        candidateKeys.add(keys);
        continue;
      }
      for(Set<String> candidate:candidateKeys){
        if(keys.size() < candidate.size()){
          if(candidate.containsAll(keys)){
            candidateKeys.remove(candidate);
          }
          candidateKeys.add(keys);
        }else{
          if(!keys.containsAll(candidate)){
            candidateKeys.add(keys);
          }
        }
      }
    }
    return candidateKeys;
  }

}