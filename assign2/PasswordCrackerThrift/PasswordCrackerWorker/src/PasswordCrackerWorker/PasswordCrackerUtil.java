package PasswordCrackerWorker;

import org.apache.thrift.TException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static PasswordCrackerWorker.PasswordCrackerConts.PASSWORD_CHARS;
import static PasswordCrackerWorker.PasswordCrackerConts.PASSWORD_LEN;

public class PasswordCrackerUtil {

	private static MessageDigest getMessageDigest() {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot use MD5 Library:" + e.getMessage());
		}
	}

	private static String encrypt(String password, MessageDigest messageDigest) {
		messageDigest.update(password.getBytes());
		byte[] hashedValue = messageDigest.digest();
		return byteToHexString(hashedValue);
	}

	private static String byteToHexString(byte[] bytes) {
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

	/*
	 * The findPasswordInRange method finds the password.
	 * if it finds the password, it set the termination for transferring signal to master and returns password to caller.
	 */
	public static String findPasswordInRange(long rangeBegin, long rangeEnd, String encryptedPassword, TerminationChecker terminationChecker) throws TException, InterruptedException {
		/** COMPLETE **/
		int[] arr_in_base_36 = new int[PASSWORD_LEN];
		String cand_passwd, md5_cand_passwd;
		MessageDigest md = getMessageDigest();

		int term_count = 0;
		while(rangeBegin <= rangeEnd) {
			if (term_count >= 10) {
				if (terminationChecker.isTerminated()) {
					System.out.println("[DEBUG] The worker is Early Terminated");
					break;
				}
				else term_count = 0;
			}
			transformDecToBase36(rangeBegin++,arr_in_base_36);
			cand_passwd = transformIntoStr(arr_in_base_36);
			md5_cand_passwd = encrypt(cand_passwd,md);

			if(encryptedPassword.equals(md5_cand_passwd)) {
				terminationChecker.setTerminated();
				return cand_passwd;
			}
			term_count++;
		}

		return null;
	}

	/* ###  transformDecToBase36  ###
	 * The transformDecToBase36 transforms decimal into numArray that is base 36 number system
	 * If you don't understand, refer to the homework01 overview
	 */
	private static void transformDecToBase36(long numInDec, int[] numArrayInBase36) {
		/** COMPLETE **/
		int curr_index = numArrayInBase36.length - 1;

		while(curr_index >= 0) {
			numArrayInBase36[curr_index--] = (int) (numInDec % 36);
			numInDec /= 36;
		}

		return;
	}

	//  ### getNextCandidate ###
	private static void getNextCandidate(int[] candidateChars) {
		/** OPTIONAL **/
	}

	private static String transformIntoStr(int[] chars) {
		char[] password = new char[chars.length];
		for (int i = 0; i < password.length; i++) {
			password[i] = PASSWORD_CHARS.charAt(chars[i]);
		}
		return new String(password);
	}
}
