package PasswordCrackerMaster;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TSocket;
import thrift.gen.PasswordCrackerMasterService.PasswordCrackerMasterService;
import thrift.gen.PasswordCrackerWorkerService.PasswordCrackerWorkerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static PasswordCrackerMaster.PasswordCrackerConts.TOTAL_PASSWORD_RANGE_SIZE;
import static PasswordCrackerMaster.PasswordCrackerConts.SUB_RANGE_SIZE;
import static PasswordCrackerMaster.PasswordCrackerConts.WORKER_PORT;
import static PasswordCrackerMaster.PasswordCrackerMasterServiceHandler.jobInfoMap;
import static PasswordCrackerMaster.PasswordCrackerMasterServiceHandler.workersAddressList;

public class PasswordCrackerMasterServiceHandler implements PasswordCrackerMasterService.Iface {
	public static List<TSocket> workersSocketList = new LinkedList<>();  //Connected Socket
	public static List<String> workersAddressList = new LinkedList<>(); // Connected WorkerAddress
	public static ConcurrentHashMap<String, PasswordDecrypterJob> jobInfoMap = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, Long> latestHeartbeatInMillis = new ConcurrentHashMap<>(); // <workerAddress, time>
	public static ExecutorService workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
	public static ScheduledExecutorService heartBeatCheckPool = Executors.newScheduledThreadPool(1);

	/*
	 * The decrypt method create the job and put the job with jobId (encrypted Password) in map.
	 * And call the requestFindPassword and if it finds the password, it return the password to the client.
	 */
	@Override
		public String decrypt(String encryptedPassword)
		throws TException {
			PasswordDecrypterJob decryptJob = new PasswordDecrypterJob();
			jobInfoMap.put(encryptedPassword, decryptJob);

			/** COMPLETE **/
			System.out.println("[DEBUG] Find Password : " + encryptedPassword);

			requestFindPassword(encryptedPassword,0,SUB_RANGE_SIZE);

			String passwd = decryptJob.getPassword();

			System.out.println("[DEBUG] The password is : " + passwd);

			return passwd;
		}

	/*
	 * The reportHeartBeat receives the heartBeat from workers.
	 * Consider the checkHeartBeat method and use latestHeartbeatInMillis map.
	 */
	@Override
		public void reportHeartBeat(String workerAddress)
		throws TException {
			/** COMPLETE **/
			long curr_time = System.currentTimeMillis();
			if(latestHeartbeatInMillis.containsKey(workerAddress))
				latestHeartbeatInMillis.replace(workerAddress,curr_time);
			else
				latestHeartbeatInMillis.put(workerAddress,curr_time);
		}

	/*
	 * The requestFindPassword requests workers to find password using RPC in asynchronous way.
	 */
	public static void requestFindPassword(String encryptedPassword, long rangeBegin, long subRangeSize) {
		PasswordCrackerWorkerService.AsyncClient worker = null;
		FindPasswordMethodCallback findPasswordCallBack = new FindPasswordMethodCallback(encryptedPassword);
		try {
			int workerId = 0;
			for (String workerAddress : workersAddressList) {
				System.out.println("[DEBUG] Request at worker : " + workerId + " , Address : " + workerAddress + " , Index : " + workersAddressList.indexOf(workerAddress));

				long subRangeBegin = rangeBegin + (workerId * subRangeSize);
				long subRangeEnd = subRangeBegin + subRangeSize;

				worker = new PasswordCrackerWorkerService.AsyncClient(new TBinaryProtocol.Factory(), new TAsyncClientManager(), new TNonblockingSocket(workerAddress, WORKER_PORT));
				worker.startFindPasswordInRange(subRangeBegin, subRangeEnd, encryptedPassword, findPasswordCallBack);
				workerId++;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (TException e) {
			e.printStackTrace();
		}
	}

	/*
	 * The redistributeFailedTask distributes the dead workers's job (or a set of possible password) to active workers.
	 *
	 * Check the checkHeartBeat method
	 */
	public static void redistributeFailedTask(ArrayList<Integer> failedWorkerIdList) {
		/** COMPLETE **/
		int worker_size = workersAddressList.size();
		long subRangeSize = (TOTAL_PASSWORD_RANGE_SIZE + worker_size - 1) / worker_size;
		long curr_subrange_size = (subRangeSize + worker_size - 1) / worker_size;
		for(Integer workerId : failedWorkerIdList) {
			long range_begin = workerId * subRangeSize;
			System.out.println("[DEBUG] Dead(workerId,rangebegin,subrangesize):"+workerId+","+range_begin+","+curr_subrange_size);
			for(String encryptedPassword : jobInfoMap.keySet()) {
				requestFindPassword(encryptedPassword,range_begin,curr_subrange_size);
			}
		}
	}

	/*
	 *  If the master didn't receive the "HeartBeat" in 5 seconds from any workers,
	 *  it considers the workers that didn't send the "HeartBeat" as dead.
	 *  And then, it redistributes the dead workers's job in other alive workers
	 *
	 *  hint : use latestHeartbeatInMillis, workerAddressList
	 *
	 *  you must think about when several workers is dead.
	 *
	 *  and use the workerPool
	 */
	public static void checkHeartBeat() {
		/** COMPLETE **/
		int workerId = 0;
		final long thresholdAge = 5_000;

		ArrayList<Integer> failedWorkerIdList = new ArrayList<>();

		for(String workerAddress : workersAddressList) {
			if(!latestHeartbeatInMillis.containsKey(workerAddress)) {
				workerId++;
				continue;
			}
			long curr_time = System.currentTimeMillis();
			if(curr_time - latestHeartbeatInMillis.get(workerAddress) > thresholdAge) {
				System.out.println("[DEBUG] workerAddress : " + workerAddress + " , workerId : " + workerId);
				latestHeartbeatInMillis.remove(workerAddress);
				failedWorkerIdList.add(workerId);
				workersSocketList.get(workerId).close();
				workersSocketList.remove(workerId);
			}
			workerId++;
		}

		for(int idx = 0; idx < failedWorkerIdList.size(); idx++)
				workersAddressList.remove((int)failedWorkerIdList.get(idx));

		if(!failedWorkerIdList.isEmpty()) {
			System.out.println("[DEBUG] Redistribution call");
			redistributeFailedTask(failedWorkerIdList);
		}
	}
}

//CallBack
class FindPasswordMethodCallback implements AsyncMethodCallback<PasswordCrackerWorkerService.AsyncClient.startFindPasswordInRange_call> {
	private String jobId;

	FindPasswordMethodCallback(String jobId) {
		this.jobId = jobId;
	}

	/*
	 *  if the returned result from worker is not null, it completes the job.
	 *  and call the jobTermination method
	 */
	@Override
		public void onComplete(PasswordCrackerWorkerService.AsyncClient.startFindPasswordInRange_call startFindPasswordInRange_call) {
			try {
				String findPasswordResult = startFindPasswordInRange_call.getResult();
				/** COMPLETE **/
				if(findPasswordResult != null) {
					System.out.println("[DEBUG] Password Found!!");
					jobInfoMap.get(jobId).setPassword(findPasswordResult);
					jobTermination(jobId);
					jobInfoMap.remove(jobId);
				}
			}
			catch (TException e) {
				e.printStackTrace();
			}
		}

	@Override
		public void onError(Exception e) {
			System.out.println("Error : startFindPasswordInRange of FindPasswordMethodCallback");
		}

	/*
	 *  The jobTermination transfer the termination signal to workers in asynchronous way
	 */
	private void jobTermination(String jobId) {
		System.out.println("[DEBUG] Job Termination!!!");
		try {
			PasswordCrackerWorkerService.AsyncClient worker = null;
			for (String workerAddress : workersAddressList) {
				worker = new PasswordCrackerWorkerService.
					AsyncClient(new TBinaryProtocol.Factory(), new TAsyncClientManager(), new TNonblockingSocket(workerAddress, WORKER_PORT));
				/** COMPLETE **/
				worker.reportTermination(jobId,null);
			}
		}
		catch (TException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
