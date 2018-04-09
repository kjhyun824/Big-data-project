package PasswordCrackerMaster;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PasswordDecrypterJob {
	// if you want to know CompletableFuture class,
	// refer to the site; https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html

	CompletableFuture<String> passwordFuture = new CompletableFuture<>();


	/**
	 * get()
	 * Waits if necessary for this future to complete, and then
	 * returns its result.
	 *
	 * @return the result value
	 */
	public String getPassword() {
		/** COMPLETE **/

		String result_p = null;
		try {
			result_p = passwordFuture.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return result_p;
	}


	/**
	 * complete(T value)
	 * If not already completed, sets the value returned by get() and related methods to the given value.
	 * param : value the result value
	 *
	 * @return {@code true} if this invocation caused this CompletableFuture
	 * to transition to a completed state, else {@code false}
	 */
	public void setPassword(String password) {
		/** COMPLETE **/
		passwordFuture.complete(password);
	}

}



