import java.util.*;

/**
 * This utility class is not meant to be instantitated, and just provides some
 * useful methods on FD sets.
 * 
 * @author Lucas Gover
 * @version 10 / 25 / 2022
 */
public final class FDUtil {

  /**
   * Resolves all trivial FDs in the given set of FDs
   * 
   * @param fdset (Immutable) FD Set
   * @return a set of trivial FDs with respect to the given FDSet
   */
  public static FDSet trivial(final FDSet fdset) {
    // Obtain the power set of each FD's left-hand attributes. For each
    // element in the power set, create a new FD and add it to the a new FDSet.
    FDSet newFDSet = new FDSet();
    for (FD current : fdset) { // for every FD in the FDSet,
      Set<Set<String>> pset = powerSet(current.getLeft()); // get power set of each FD's left hand attr.
      for (Set<String> next : pset) {
        if (next.size() > 0) { // don't add {"A","B"} --> {}
          newFDSet.add(new FD(current.getLeft(), next)); // make a trivial FD
        }
      }
    }

    return newFDSet;
  }

  /**
   * Augments every FD in the given set of FDs with the given attributes
   * 
   * @param fdset FD Set (Immutable)
   * @param attrs a set of attributes with which to augment FDs (Immutable)
   * @return a set of augmented FDs
   */
  public static FDSet augment(final FDSet fdset, final Set<String> attrs) {
    // Copy each FD in the given set and then union both sides with the given
    // set of attributes, and add this augmented FD to a new FDSet.
    FDSet newSet = new FDSet();
    for (FD next : fdset) { // for each FD in FDSet
      FD newFD = new FD(next);
      newFD.addToLeft(attrs); // augment the value to both sides
      newFD.addToRight(attrs);
      newSet.add(newFD);
    }
    return newSet;
  }

  /**
   * Exhaustively resolves transitive FDs with respect to the given set of FDs
   * 
   * @param fdset (Immutable) FD Set
   * @return all transitive FDs with respect to the input FD set
   */
  public static FDSet transitive(final FDSet fdset) {
    // Examine each pair of FDs in the given set. If the transitive property
    // holds on the pair of FDs, then generate the new FD and add it to a new FDSet.
    // Repeat until no new transitive FDs are found.
    FDSet newSet = new FDSet();
    Iterator<FD> iter1 = fdset.iterator();
    FD left;
    FD right;
    FD newFD;
    int startSize;
    do{
      startSize = newSet.size();
      while(iter1.hasNext()){
        Iterator<FD> iter2 = fdset.iterator();
        left = iter1.next();
        while(iter2.hasNext()){
          right = iter2.next();
          if(left.getRight().equals(right.getLeft())){
            newFD = new FD(left.getLeft(),right.getRight()); 
            newSet.add(newFD);
          }
        }
      }
    }while(newSet.size() > startSize);
    return newSet;
  }

  /**
   * Generates the closure of the given FD Set
   * 
   * @param fdset (Immutable) FD Set
   * @return the closure of the input FD Set
   */
  public static FDSet fdSetClosure(final FDSet fdset) {
    FDSet newSet = new FDSet(fdset); //Use the FDSet copy constructor to deep copy the given FDSet
    int startSize;
    //Generate new FDs by applying Trivial and Augmentation Rules, followed
    do{
      startSize  = newSet.size();
      newSet.addAll(trivial(newSet));
      Set<Set<String>> pSetOfSingletons = powerSet(getSingletons(fdset));
      for(Set<String> subset : pSetOfSingletons) {
        newSet.addAll(augment(newSet, subset));
      }
    }while(startSize < newSet.size()); // Repeat until no further changes are detected.
    newSet.addAll(transitive(newSet)); // by Transitivity Rule, and add new FDs to the result.

    return newSet;
  }

  /**
   * Generates the power set of the given set (that is, all subsets of
   * the given set of elements)
   * 
   * @param set Any set of elements (Immutable)
   * @return the power set of the input set
   */
  @SuppressWarnings("unchecked")
  public static <E> Set<Set<E>> powerSet(final Set<E> set) {

    // base case: power set of the empty set is the set containing the empty set
    if (set.size() == 0) {
      Set<Set<E>> basePset = new HashSet<>();
      basePset.add(new HashSet<>());
      return basePset;
    }

    // remove the first element from the current set
    E[] attrs = (E[]) set.toArray();
    set.remove(attrs[0]);

    // recurse and obtain the power set of the reduced set of elements
    Set<Set<E>> currentPset = FDUtil.powerSet(set);

    // restore the element from input set
    set.add(attrs[0]);

    // iterate through all elements of current power set and union with first
    // element
    Set<Set<E>> otherPset = new HashSet<>();
    for (Set<E> attrSet : currentPset) {
      Set<E> otherAttrSet = new HashSet<>(attrSet);
      otherAttrSet.add(attrs[0]);
      otherPset.add(otherAttrSet);
    }
    currentPset.addAll(otherPset);
    return currentPset;
  }
  private static Set<String> getSingletons(FDSet f){
    HashSet<String> newSet = new HashSet<>();
    for(FD fd:f){
      newSet.addAll(fd.getLeft());
      newSet.addAll(fd.getRight());
    }
    return newSet;
  }
}