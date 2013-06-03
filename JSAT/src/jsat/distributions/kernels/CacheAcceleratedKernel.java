package jsat.distributions.kernels;

import java.util.List;
import jsat.linear.Vec;

/**
 * Some kernels can be more expensive to evaluate than others, which can be 
 * particularly problematic during training time. This interface provides 
 * kernels the ability to expose and use a cache of values to accelerate kernel 
 * computation when working with a fixed training set. The cache should be 
 * linear in size compared to the training set size. This assumption is made for
 * sanity in memory use, and is not enforcable. <br><br>
 * This cache should not be used to store an explicit kernel map for every 
 * combination, which is caching all results instead of accelerating results. 
 * Such extreme caching strategies should be handled by the underlying algorithm. 
 * 
 * @author Edward Raff
 */
public interface CacheAcceleratedKernel extends KernelTrick
{
    /**
     * Creates a new list cache values from a given list of training set 
     * vectors.
     *
     * @param trainingSet the list of training set vectors
     * @return a list of cache values that may be used by this kernel
     */
    public List<Double> getCache(List<? extends Vec> trainingSet);
    
    /**
     * Appends the new cache values for the given vector to the list of cache 
     * values. This method is present for online style kernel learning 
     * algorithms, where the set of vectors is not known in advance. When a 
     * vector is added to the set of kernel vectors, its cache values can be 
     * added using this method. <br><br>
     * The results of calling this sequentially on a lit of vectors starting
     * with an empty double list is equivalent to getting the results from 
     * calling {@link #getCache(java.util.List) }
     * 
     * @param newVec the new vector to add to the cache values
     * @param cache the original list of cache values to add to
     */
    public void addToCache(Vec newVec, List<Double> cache);
    
    /**
     * Produces the correct kernel evaluation given the training set and the
     * cache generated by {@link #getCache(jsat.linear.Vec[]) }. The training
     * vectors should be in the same order.
     * 
     * @param a the index of the first training vector
     * @param b the index of the second training vector
     * @param trainingSet the list of training set vectors
     * @param cache the double list of cache values generated by this kernel 
     * for the given training set
     * @return the same kernel evaluation result as 
     * {@link #eval(jsat.linear.Vec, jsat.linear.Vec) }
     */
    public double eval(int a, int b, List<? extends Vec> trainingSet, List<Double> cache);
    
    /**
     * Performs an efficient summation of kernel products of the form <br>
     * <big>&#8721;</big> &alpha;<sub>i</sub> k(x<sub>i</sub>, y) <br>
     * where <i>x</i> are the final set of vectors, and <i>&alpha;</i> the 
     * associated scalar multipliers
     * 
     * @param finalSet the final set of vectors
     * @param cache the cache associated with the final set of vectors
     * @param alpha the coefficients associated with each vector
     * @param y the vector to perform the summed kernel products against
     * @param start the starting index (inclusive) to sum from
     * @param end the ending index (exclusive) to sum from
     * @return the sum of the multiplied kernel products
     */
    public double evalSum(List<? extends Vec> finalSet, List<Double> cache, double[] alpha, Vec y, int start, int end);
    
}
