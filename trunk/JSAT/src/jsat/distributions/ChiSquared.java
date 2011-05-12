
package jsat.distributions;

import jsat.linear.Vec;

import static java.lang.Math.*;
import static jsat.math.SpecialMath.*;

/**
 *
 * @author Edward Raff
 */
public class ChiSquared extends ContinousDistribution
{
    double df;//Degrees of freedom

    public ChiSquared(double df)
    {
        this.df = df;
    }

    
    @Override
    public double pdf(double x)
    {
        if(x < 0)
            return 0;
        /*
         *   df      -x
         *   -- - 1  --
         *    2       2
         *  x       e
         * -------------
         *  df
         *  --
         *   2      /df\
         * 2   Gamma|--|
         *          \ 2/
         */
        
        return exp((df/2-1)*log(x)-x/2-df/2*log(2)+lnGamma(df/2));
    }


    @Override
    public double cdf(double x)
    {
        if(df == 2)//special case with a closed form that is more accurate to compute, we include it b/c df = 2 is not uncomon
            return 1-exp(-x/2);
        return gammaP(df/2, x/2);
    }

    @Override
    public double invCdf(double p)
    {
        if(df == 2)//special case with a closed form that is more accurate to compute, we include it b/c df = 2 is not uncomon
            return 2*log(1-p);
        return 2* invGammaP(p, df/2);
    }

    @Override
    public double min()
    {
        return 0;
    }

    @Override
    public double max()
    {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public String getDistributionName()
    {
        return "Chi^2";
    }

    @Override
    public String[] getVariables()
    {
        return new String[] {"df"};
    }

    @Override
    public double[] getCurrentVariableValues()
    {
        return new double[] {df};
    }

    @Override
    public void setVariable(String var, double value)
    {
        if(var.equals("df"))
            df = value;
    }

    @Override
    public ContinousDistribution copy()
    {
        return new ChiSquared(df);
    }

    @Override
    public void setUsingData(Vec data)
    {
        df = ceil(data.variance()/2);
    }

    @Override
    public double mean()
    {
        return df;
    }

    @Override
    public double median()
    {
        //2*InvGammaP(df/2,1/2)
        return invGammaP(df/2, 0.5)*2;
    }

    @Override
    public double mode()
    {
        return Math.max(df-2, 0.0);
    }

    @Override
    public double variance()
    {
        return 2 * df;
    }

    
}
