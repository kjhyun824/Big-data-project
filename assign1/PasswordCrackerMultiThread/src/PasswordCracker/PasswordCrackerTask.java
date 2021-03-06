package PasswordCracker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static PasswordCracker.PasswordCrackerConsts.*;
import java.lang.*;

// refer to Runnable class
// site : https://docs.oracle.com/javase/8/docs/api/java/lang/Runnable.html

public class PasswordCrackerTask implements Runnable {
	int taskId;
	boolean isEarlyTermination;
	PasswordFuture passwordFuture;
	PasswordCrackerConsts consts;

	public PasswordCrackerTask(int taskId, boolean isEarlyTermination, PasswordCrackerConsts consts, PasswordFuture passwordFuture) {
		this.taskId = taskId;
		this.isEarlyTermination = isEarlyTermination;
		this.consts = consts;
		this.passwordFuture = passwordFuture;
	}

	/* ### run ###

	 */
	@Override
		public void run() {
			/** COMPLETE **/
			long rangeBegin = taskId*consts.getPasswordSubRangeSize();
			long rangeEnd = consts.numThreads != (taskId + 1) ? ((taskId + 1) * consts.getPasswordSubRangeSize()) - 1 : consts.getPasswordTotalRangeSize() - 1;
			String passwordOrNull = findPasswordInRange(rangeBegin, rangeEnd, consts.getEncryptedPassword());

			if(passwordOrNull == null) return;

			passwordFuture.set(passwordOrNull);

			return;
		}

	/*	### findPasswordInRange	###
	 * The findPasswordInRange method find the original password using md5 hash function
	 * if a thread discovers the password, it returns original password string; otherwise, it returns null;
	 */
	public String findPasswordInRange(long rangeBegin, long rangeEnd, String encryptedPassword) {
		/** COMPLETE **/
		int p_length = consts.getPasswordLength();
		int count_for_done = 0;
		int[] arr_in_base_36 = new int[p_length];
		String cand_passwd, md5_cand_passwd;
		MessageDigest md = getMessageDigest();

		while(rangeBegin <= rangeEnd) {
			if(this.isEarlyTermination) {
				if(count_for_done >= 10) {
					if (passwordFuture.isDone()) break;
					count_for_done = 0;
				}
				count_for_done++;
			}

			transformDecToBase36(rangeBegin++,arr_in_base_36);
			cand_passwd = transformIntToStr(arr_in_base_36);
			md5_cand_passwd = encrypt(cand_passwd,md);

			if(encryptedPassword.equals(md5_cand_passwd))	return cand_passwd;
		}

		return null;
	}

	/* ###	transformDecToBase36  ###
	 * The transformDecToBase36 transforms decimal into numArray that is base 36 number system
	 * If you don't understand, refer to the homework01 overview
	 */
	private static void transformDecToBase36(long numInDec, int[] numArrayInBase36) {
		/** COMPLETE **/

		int curr_index = numArrayInBase36.length-1;

		while(curr_index >= 0) {
			numArrayInBase36[curr_index--] = (int) (numInDec % 36);
			numInDec /= 36;
		}

		return;
	}

	/*
	 * The getNextCandidate update the possible password represented by 36 base system
	 */
	private static void getNextCandidate(int[] candidateChars) {
		/** COMPLETE **/


	}

	/*
	 * We assume that each character can be represented to a number : 0 (0) , 1 (1), 2 (2) ... a (10), b (11), c (12), ... x (33), y (34), z (35)
	 * The transformIntToStr transforms int-array into string (numbers and lower-case alphabets)
	 * int array is password represented by base-36 system
	 * return : password String
	 *
	 * For example, if you write the code like this,
	 *     int[] pwdBase36 = {10, 11, 12, 13, 0, 1, 9, 2};
	 *     String password = transfromIntoStr(pwdBase36);
	 *     System.out.println(password);
	 *     output is abcd0192.
	 *
	 */
	private static String transformIntToStr(int[] chars) {
		char[] password = new char[chars.length];
		for (int i = 0; i < password.length; i++) {
			password[i] = PASSWORD_CHARS.charAt(chars[i]);
		}
		return new String(password);
	}


	public static MessageDigest getMessageDigest() {
		try {
			return MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot use MD5 Library:" + e.getMessage());
		}
	}

	public static String encrypt(String password, MessageDigest messageDigest) {
		messageDigest.update(password.getBytes());
		byte[] hashedValue = messageDigest.digest();
		return byteToHexString(hashedValue);
	}

	public static String byteToHexString(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				builder.append('0');
			}
			builder.append(hex);
		}
		return builder.toString();
	}
}



