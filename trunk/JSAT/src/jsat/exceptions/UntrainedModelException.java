
package jsat.exceptions;

/**
 * This exception is thrown when someone attempts to use a model that has not been trained or constructed. 
 * @author Edward Raff
 */
public class UntrainedModelException extends RuntimeException
{

    public UntrainedModelException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UntrainedModelException(Throwable cause)
    {
        super(cause);
    }

    public UntrainedModelException(String message)
    {
        super(message);
    }

    public UntrainedModelException()
    {
        super();
    }
    
}
