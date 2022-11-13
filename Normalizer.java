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
    System.out.println("Decomposing!");
    // First test if the given relation is already in BCNF with respect to
    // the provided FD set.
    if(isBCNF(rel,fdset)){
      return new HashSet<Set<String>>(Set.of(rel));
    }
    // Identify a nontrivial FD that violates BCNF. Split the relation's
    // attributes using that FD, as seen in class.
    Set<Set<String>> superKeys = findSuperkeys(rel,fdset);
    System.out.println("Superkeys:" + superKeys);
    FD violatingFD = null;
    for(FD fd :fdset){
      if(!isTrivial(fd) && !superKeys.contains(fd.getLeft())){
        violatingFD = fd;
        System.out.println(violatingFD);
        break;
      }
    }

    // TODO - Redistribute the FDs in the closure of fdset to the two new
    // relations (R_Left and R_Right) as follows:
    Set<String>R_Left = new HashSet<String>(violatingFD.getLeft());
    R_Left.addAll(violatingFD.getRight());
    Set<String>R_Right = new HashSet<String>(rel);
    R_Right.removeAll(violatingFD.getRight());
    R_Right.addAll(violatingFD.getLeft());
    //
    FDSet closure = FDUtil.fdSetClosure(fdset);
    FDSet fds1 = new FDSet();
    FDSet fds2 = new FDSet();

    for(FD fd:closure){
      if(R_Left.containsAll(fd.getLeft()) && R_Left.containsAll(fd.getRight())){
        fds1.add(fd);
      }
      if(R_Right.containsAll(fd.getLeft()) && R_Right.containsAll(fd.getRight())){
        fds2.add(fd);
      }
    }
    // Iterate through closure of the given set of FDs, then union all attributes
    // appearing in the FD, and test if the union is a subset of the R_Left (or
    // R_Right) relation. If so, then the FD gets added to the R_Left's (or R_Right's) FD
    // set. If the union is not a subset of either new relation, then the FD is
    // discarded

    // Repeat the above until all relations are in BCNF
    System.out.println("R1: " + R_Left);
    System.out.println("R2: " + R_Right);
    System.out.println("fds1: " + fds1.toString());
    System.out.println("fds2: " + fds2.toString());
    Set<Set<String>> returnValue = BCNFDecompose(R_Left,fds1);
    returnValue.addAll(BCNFDecompose(R_Right,fds2));
    return returnValue;
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
    Set<Set<String>> keySet = new HashSet<Set<String>>();
    for(Set<String> subset: FDUtil.powerSet(rel)){
      System.out.println(subset);
      System.out.println(FDUtil.fdSetClosure(retainedFDs(subset,fdset)));
      System.out.println("FDSet: " + fdset);
      if(FDUtil.fdSetClosure(retainedFDs(subset,fdset)).size() >= fdset.size()){
        keySet.add(subset);
      }
    }
    return keySet;
  }
  private static FDSet retainedFDs(Set<String> rel, FDSet fdset){
    FDSet retained = new FDSet();
    for(FD fd : fdset){
      if(rel.containsAll(fd.getLeft())){
        retained.add(fd);
      }
    }
    return retained;
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