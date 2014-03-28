package jsat.classifiers.evaluation;

import jsat.classifiers.evaluation.Precision;
import jsat.classifiers.CategoricalData;
import jsat.classifiers.CategoricalResults;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Edward Raff
 */
public class PrecisionTest
{
    
    public PrecisionTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getScore method, of class Precision.
     */
    @Test
    public void testGetScore()
    {
        System.out.println("getScore");
        Precision scorer = new Precision();
        
        scorer.prepareMetric(new CategoricalData(2));
        //correct
        scorer.addResult(new CategoricalResults(new double[]{1.0, 0.0}), 0, 1.0);
        scorer.addResult(new CategoricalResults(new double[]{0.2, 0.8}), 1, 3.0);
        scorer.addResult(new CategoricalResults(new double[]{7.0, 0.3}), 0, 1.0);
        //wrong
        scorer.addResult(new CategoricalResults(new double[]{0.6, 0.4}), 1, 1.0);
        scorer.addResult(new CategoricalResults(new double[]{0.4, 0.6}), 0, 2.0);
        scorer.addResult(new CategoricalResults(new double[]{0.9, 0.1}), 1, 1.0);
        
        double tp = 2, tn = 3, fp = 2, fn = 2;
        assertEquals(tp/(tp*+fp), scorer.getScore(), 1e-2);
    }

}
