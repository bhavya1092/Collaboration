package com.niit.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.niit.dao.JobDAO;
import com.niit.model.BaseDomain;
import com.niit.model.Job;
import com.niit.model.JobApplication;

@RestController
public class JobController {

private static final Logger logger	= LoggerFactory.getLogger(JobController.class);

    @Autowired
    BaseDomain baseDomain;
	
	@Autowired
	JobDAO jobDAO;
	
	@Autowired
	JobApplication jobApplication;
	
	@RequestMapping(value="/jobs",method=RequestMethod.GET)
	public ResponseEntity<List<Job>> listAllJobs(){
		logger.debug("calling method listAllJobs");
		List<Job> job=jobDAO.list();
		if(job.isEmpty()){
			Job j=new Job();
			j.setErrorCode("404");
		    j.setErrorMessage("No jobs are available");			
			return new ResponseEntity<List<Job>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Job>>(job,HttpStatus.OK);
	}

	@RequestMapping(value="/job/",method=RequestMethod.POST)
	public ResponseEntity<Job> createJob(@RequestBody Job job){
		logger.debug("calling method createJob" + job.getJob_id());
		//job.setStatus('V');
		if(jobDAO.save(job)==true){
			baseDomain.setErrorCode("200");
			baseDomain.setErrorMessage("job posted");			
		}else{
		logger.debug("job cannot be posted");
		baseDomain.setErrorCode("400");
		baseDomain.setErrorMessage("job cannot be posted");
		return new ResponseEntity<Job>(job,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Job>(job,HttpStatus.OK); //new means returning instance without that it means
		//we are returning class not possible
			}
	
	@RequestMapping(value="/job/{id}",method=RequestMethod.PUT)
	public ResponseEntity<Job> updateJob(@PathVariable("id") int job_id,@RequestBody Job job){
		logger.debug("calling method updateJob" + job.getJob_id());
		System.out.println("starting to update"+ " " + job_id);
		if(jobDAO.get(job_id)==null){
			logger.debug("job does not exists with id:" + job.getJob_id());		
			job=new Job();
			job.setErrorMessage("job does not exists with id:" + job.getJob_id());
			return new ResponseEntity<Job> (job,HttpStatus.NOT_FOUND);
		}
		System.out.println("updating" + " " + job_id);
		jobDAO.update(job);
		logger.debug("job updated successfully");
		System.out.println("updating end" + " " + job_id);
		return new ResponseEntity<Job> (job,HttpStatus.OK);		
	}

	
	@RequestMapping(value="/job/{id}",method=RequestMethod.GET)
	public ResponseEntity<Job> getJob(@PathVariable("id") int job_id){
		logger.debug("calling method getJob for job id: " + job_id);
		Job job=jobDAO.get(job_id);
		if(job==null){
			logger.debug("job does not exists with id:" + job_id);
			job=new Job();
			job.setErrorMessage("job does not exists with id:" + job_id);
			return new ResponseEntity<Job> (job,HttpStatus.NOT_FOUND);
		}
		logger.debug("job exists with id:" + job_id);
		return new ResponseEntity<Job> (job,HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value="/getMyAppliedJobs/",method=RequestMethod.GET)
	public ResponseEntity<List<Job>> getMyAppliedJobs(HttpSession httpSession){
		logger.debug("calling method getMyAppliedJobs");
		//String loogedInUserID=(String)httpSession.getAttribute("loogedInUserID");
		int loogedInUserID=(int) httpSession.getAttribute("loogedInUserID");
		List<Job> jobs=(List<Job>) jobDAO.getMyAppliedJobs(loogedInUserID);
		return new ResponseEntity<List<Job>> (jobs,HttpStatus.OK);
		
	}
	
	@RequestMapping(value="/getJobDetails/{id}",method=RequestMethod.GET)
	public ResponseEntity<Job> getJobDetails(@RequestParam("jobID") int job_id){
		logger.debug("calling method getJobDetails");
		Job job=jobDAO.get(job_id);
		return new ResponseEntity<Job> (job,HttpStatus.OK);	
	}
	
	@RequestMapping(value="/applyForJob/{id}",method=RequestMethod.GET)
	public ResponseEntity<JobApplication> applyForJob(@RequestParam("jobID") int job_id,HttpSession httpSession){
		logger.debug("calling method applyForJob");	
		//String loogedInUserID=(String)httpSession.getAttribute("loogedInUserID");
				int loogedInUserID=(int) httpSession.getAttribute("loogedInUserID");
				
				jobApplication=jobDAO.getJobApplication(job_id);
				jobApplication.setUser_id(loogedInUserID);
				jobApplication.setStatus('N');
				if(jobDAO.applyForJob(jobApplication)){
					jobApplication.setErrorCode("200");
					jobApplication.setErrorMessage("Job Applied");
					logger.debug("Succesfully applied");
				}
				return new ResponseEntity<JobApplication>(jobApplication,HttpStatus.OK);
	}
	
	@RequestMapping(value="/selectUser/{userID}/{jobID}",method=RequestMethod.PUT)
	public ResponseEntity<JobApplication> selectUser(@RequestParam("userID") int userID,@RequestParam("jobID") int jobID ){
		logger.debug("calling method selectUser");
		jobApplication.setStatus('S');
		if(jobDAO.applyForJob(jobApplication)){
			jobApplication.setErrorCode("200");
			jobApplication.setErrorMessage("Candidate Selected");
			logger.debug("Succesfully selected");
		}
		return new ResponseEntity<JobApplication>(jobApplication,HttpStatus.OK);
}
	
	@RequestMapping(value="/canCallForInterview/{userID}/{jobID}",method=RequestMethod.PUT)
	public ResponseEntity<JobApplication> canCallForInterview(@RequestParam("userID") int userID,@RequestParam("jobID") int jobID ){
		logger.debug("calling method canCallForInterview");
		jobApplication.setStatus('C');
		if(jobDAO.applyForJob(jobApplication)){
			jobApplication.setErrorCode("200");
			jobApplication.setErrorMessage("Applicant called for interview");
			logger.debug("Succesfully called");
		}
		return new ResponseEntity<JobApplication>(jobApplication,HttpStatus.OK);
}
	
	@RequestMapping(value="/rejectJobApplication/{userID}/{jobID}",method=RequestMethod.PUT)
	public ResponseEntity<JobApplication> rejectJobApplication(@RequestParam("userID") int userID,@RequestParam("jobID") int jobID ){
		logger.debug("calling method rejectJobApplication");
		jobApplication.setStatus('R');
		if(jobDAO.applyForJob(jobApplication)){
			jobApplication.setErrorCode("200");
			jobApplication.setErrorMessage("Applicant Rejected");
			logger.debug("Succesfully rejected");
		}
		return new ResponseEntity<JobApplication>(jobApplication,HttpStatus.OK);
}
	
	
	
}




