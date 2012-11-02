package jsat.datatransform.featureselection;

import java.util.*;
import jsat.DataSet;
import jsat.classifiers.ClassificationDataSet;
import jsat.classifiers.Classifier;
import jsat.classifiers.DataPoint;
import jsat.datatransform.*;
import jsat.regression.RegressionDataSet;
import jsat.regression.Regressor;
import jsat.utils.ListUtils;

/**
 * plus-L minus-R Selection (LRS) is a greedy method of selecting a subset 
 * of features to use for prediction. Its behavior is dependent upon whether L 
 * or R is the larger value. No mater what, L features will be greedily added to
 * the set to decrease the error rate, and R features will be greedily removed 
 * while trying to maintain the error rate. <br>
 * If L &gt; R, then L-R features will be selected, the L step running first 
 * followed by R performing pruning on the found set. <br>
 * If L &lt; R, then D-R+L features will be selected, where D is the original 
 * number of features. First R features will be removed, and then L of the 
 * removed features will be added back to the final set. <br>
 * L = R is not allowed. 
 * 
 * @author Edward Raff
 */
public class LRS implements DataTransform
{
    private RemoveAttributeTransform finalTransform;
    private Set<Integer> catSelected;
    private Set<Integer> numSelected;
    
    /**
     * Copy constructor
     * @param toClone the version to copy
     */
    private LRS(LRS toClone)
    {
        if(toClone.catSelected != null)
        {
            this.finalTransform = toClone.finalTransform.clone();
            this.catSelected = new HashSet<Integer>(toClone.catSelected);
            this.numSelected = new HashSet<Integer>(toClone.numSelected);
        }
    }
    
    /**
     * Performs LRS feature selection for a classification problem
     * 
     * @param L the number of features to greedily add
     * @param R the number of features to greedily remove
     * @param cds the data set to perform feature selection on
     * @param evaluater the classifier to use in determining accuracy given a 
     * feature subset
     * @param folds the number of cross validation folds to use in selection
     */
    public LRS(int L, int R, ClassificationDataSet cds, Classifier evaluater, int folds)
    {
        search(cds, L, R, evaluater, folds);
    }
    
    /**
     * Performs LRS feature selection for a regression problem
     * 
     * @param L the number of features to greedily add
     * @param R the number of features to greedily remove
     * @param rds the data set to perform feature selection on
     * @param evaluater the regressor to use in determining accuracy given a 
     * feature subset
     * @param folds the number of cross validation folds to use in selection
     */
    public LRS(int L, int R, RegressionDataSet rds, Regressor evaluater, int folds)
    {
        search(rds, L, R, evaluater, folds);
    }

    @Override
    public DataPoint transform(DataPoint dp)
    {
        return finalTransform.transform(dp);
    }

    @Override
    public DataTransform clone()
    {
        return new LRS(this);
    }
    
    /**
     * Returns a copy of the set of categorical features selected by the search 
     * algorithm
     * 
     * @return the set of categorical features to use
     */
    public Set<Integer> getSelectedCategorical()
    {
        return new HashSet<Integer>(catSelected);
    }
    
    /**
     * Returns a copy of the set of numerical features selected by the search 
     * algorithm. 
     * 
     * @return the set of numeric features to use
     */
    public Set<Integer> getSelectedNumerical()
    {
        return new HashSet<Integer>(numSelected);
    }

    private void search(DataSet cds, int L, int R, Object evaluater, int folds)
    {
        int nF = cds.getNumFeatures();
        int nCat = cds.getNumCategoricalVars();
        
        catSelected = new HashSet<Integer>(nCat);
        numSelected = new HashSet<Integer>(nF-nCat);
        Set<Integer> catToRemove = new HashSet<Integer>(nCat);
        Set<Integer> numToRemove = new HashSet<Integer>(nF-nCat);
        
        Set<Integer> available = new HashSet<Integer>(nF);
        ListUtils.addRange(available, 0, nF, 1);
        
        Random rand = new Random();
        double[] pBestScore = new double[]{Double.POSITIVE_INFINITY};
        
        if (L > R)
        {
            ListUtils.addRange(catToRemove, 0, nCat, 1);
            ListUtils.addRange(numToRemove, 0, nF-nCat, 1);
            
            //Select L features
            for(int i = 0; i < L; i++)
                SFS.SFSSelectFeature(available, cds, catToRemove, numToRemove, 
                        catSelected, numSelected, evaluater, folds, 
                        rand, pBestScore, L);
            //We now restrict ourselves to the L features
            available.clear();
            available.addAll(catSelected);
            for(int i : numSelected)
                available.add(i+nCat);
            //Now remove R features from the L selected
            for(int i = 0; i < R; i++)
                SBS.SBSRemoveFeature(available, cds, catToRemove, numToRemove, 
                        catSelected, numSelected, evaluater, folds, rand, 
                        L-R, pBestScore, 0.0);
        }
        else if(L < R)
        {
            ListUtils.addRange(catSelected, 0, nCat, 1);
            ListUtils.addRange(numSelected, 0, nF-nCat, 1);
            
            //Remove R features
            for(int i = 0; i < R; i++)
                SBS.SBSRemoveFeature(available, cds, catToRemove, numToRemove, 
                        catSelected, numSelected, evaluater, folds, rand, 
                        nF-R, pBestScore, 0.0);
            
            //Now we restrict out selves to adding back the features that were removed
            available.clear();
            available.addAll(catToRemove);
            for(int i : numToRemove)
                available.add(i+nCat);
            
            //Now add L features back
            for(int i = 0; i < L; i++)
                SFS.SFSSelectFeature(available, cds, catToRemove, numToRemove, 
                        catSelected, numSelected, evaluater, folds, 
                        rand, pBestScore, R-L);
        }
        
        finalTransform = new RemoveAttributeTransform(cds, catToRemove, numToRemove);
    }
    
    static public class LRSFactory implements DataTransformFactory
    {
        private Classifier classifier;
        private Regressor regressor;
        private int featurestoAdd, featurestoRemove;

        /**
         * Creates a new LRS transform factory
         * 
         * @param evaluater the classifier to use to perform evaluation
         * @param toAdd the number of features to add
         * @param toRemove the number of features to remove
         */
        public LRSFactory(Classifier evaluater, int toAdd, int toRemove)
        {
            if(toAdd == toRemove)
                throw new RuntimeException("L and R must be different");
            this.classifier = evaluater;
            this.featurestoAdd = toAdd;
            this.featurestoRemove = toRemove;
        }
        
        /**
         * Creates a new LRS transform factory 
         * 
         * @param evaluater the regressor to use to perform evaluation
         * @param toAdd the number of features to add
         * @param toRemove the number of features to remove
         */
        public LRSFactory(Regressor evaluater, int toAdd, int toRemove)
        {
            if(toAdd == toRemove)
                throw new RuntimeException("L and R must be different");
            this.regressor = evaluater;
            this.featurestoAdd = toAdd;
            this.featurestoRemove = toRemove;
        }

        @Override
        public DataTransform getTransform(DataSet dataset)
        {
            if(dataset instanceof ClassificationDataSet)
                return new LRS(featurestoAdd, featurestoRemove, 
                        (ClassificationDataSet)dataset, classifier, 5);
            else
                return new LRS(featurestoAdd, featurestoRemove, 
                        (RegressionDataSet)dataset, regressor, 5);
        }
        
    }
}
