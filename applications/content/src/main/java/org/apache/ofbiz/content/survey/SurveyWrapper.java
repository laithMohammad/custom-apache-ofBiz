/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.content.survey;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.ofbiz.base.location.FlexibleLocation;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.GeneralException;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.base.util.template.FreeMarkerWorker;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
import org.apache.ofbiz.entity.util.EntityListIterator;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.entity.util.EntityUtil;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Survey Wrapper - Class to render survey forms
 */
public class SurveyWrapper {

	public static final String module = SurveyWrapper.class.getName();

	protected Delegator delegator = null;
	protected String responseId = null;
	protected String partyId = null;
	protected String surveyId = null;
	protected Map<String, Object> templateContext = null;
	protected Map<String, Object> passThru = null;
	protected Map<String, Object> defaultValues = null;
	protected boolean edit = false;

	protected SurveyWrapper() {
	}

	public SurveyWrapper(Delegator delegator, String responseId, String partyId, String surveyId, Map<String, Object> passThru, Map<String, Object> defaultValues) {
		this.delegator = delegator;
		this.responseId = responseId;
		this.partyId = partyId;
		this.surveyId = surveyId;
		this.setPassThru(passThru);
		this.setDefaultValues(defaultValues);
		this.checkParameters();
	}

	public SurveyWrapper(Delegator delegator, String responseId, String partyId, String surveyId, Map<String, Object> passThru) {
		this(delegator, responseId, partyId, surveyId, passThru, null);
	}

	public SurveyWrapper(Delegator delegator, String surveyId) {
		this(delegator, null, null, surveyId, null);
	}

	protected void checkParameters() {
		if (delegator == null || surveyId == null) {
			throw new IllegalArgumentException("Missing one or more required parameters (delegator, surveyId)");
		}
	}

	/**
	 * Sets the pass-thru values (hidden form fields)
	 *
	 * @param passThru
	 */
	public void setPassThru(Map<String, Object> passThru) {
		if (passThru != null) {
			this.passThru = new HashMap<String, Object>();
			this.passThru.putAll(passThru);
		}
	}

	/**
	 * Sets the default values
	 *
	 * @param defaultValues
	 */
	public void setDefaultValues(Map<String, Object> defaultValues) {
		if (defaultValues != null) {
			this.defaultValues = new HashMap<String, Object>();
			this.defaultValues.putAll(defaultValues);
		}
	}

	/**
	 * Adds an object to the FTL survey template context
	 *
	 * @param name
	 * @param value
	 */
	public void addToTemplateContext(String name, Object value) {
		if (templateContext == null) {
			templateContext = new HashMap<String, Object>();
		}
		templateContext.put(name, value);
	}

	/**
	 * Removes an object from the FTL survey template context
	 *
	 * @param name
	 */
	public void removeFromTemplateContext(String name) {
		if (templateContext != null)
			templateContext.remove(name);
	}

	/**
	 * Renders the Survey
	 *
	 * @return Writer object from the parsed Freemarker Template
	 * @throws SurveyWrapperException
	 */
	public Writer render(String templatePath) throws SurveyWrapperException {
		URL templateUrl = null;
		try {
			templateUrl = FlexibleLocation.resolveLocation(templatePath);
		} catch (MalformedURLException e) {
			throw new SurveyWrapperException(e);
		}
		if (templateUrl == null) {
			String errMsg = "Problem getting the template for Survey from URL: " + templatePath;
			Debug.logError(errMsg, module);
			throw new IllegalArgumentException(errMsg);
		}

		Writer writer = new StringWriter();
		this.render(templateUrl, writer);
		return writer;
	}

	/**
	 * Renders the Survey
	 *
	 * @param templateUrl the template URL
	 * @param writer      the write
	 * @throws SurveyWrapperException
	 */
	public void render(URL templateUrl, Writer writer) throws SurveyWrapperException {
		String responseId = this.getThisResponseId();
		GenericValue survey = this.getSurvey();
		List<GenericValue> surveyQuestionAndAppls = this.getSurveyQuestionAndAppls();
		Map<String, Object> results = this.getResults(surveyQuestionAndAppls);
		Map<String, Object> currentAnswers = null;
		if (responseId != null && canUpdate()) {
			currentAnswers = this.getResponseAnswers(responseId);
		} else {
			currentAnswers = this.getResponseAnswers(null);
		}

		Map<String, Object> sqaaWithColIdListByMultiRespId = new HashMap<String, Object>();
		for (GenericValue surveyQuestionAndAppl : surveyQuestionAndAppls) {
			String surveyMultiRespColId = surveyQuestionAndAppl.getString("surveyMultiRespColId");
			if (UtilValidate.isNotEmpty(surveyMultiRespColId)) {
				String surveyMultiRespId = surveyQuestionAndAppl.getString("surveyMultiRespId");
				UtilMisc.addToListInMap(surveyQuestionAndAppl, sqaaWithColIdListByMultiRespId, surveyMultiRespId);
			}
		}

		if (templateContext == null) {
			templateContext = new HashMap<String, Object>();
		}

		templateContext.put("partyId", partyId);
		templateContext.put("survey", survey);
		templateContext.put("surveyResults", results);
		templateContext.put("surveyQuestionAndAppls", surveyQuestionAndAppls);
		templateContext.put("sqaaWithColIdListByMultiRespId", sqaaWithColIdListByMultiRespId);
		templateContext.put("alreadyShownSqaaPkWithColId", new HashSet());
		templateContext.put("surveyAnswers", currentAnswers);
		templateContext.put("surveyResponseId", responseId);
		templateContext.put("sequenceSort", UtilMisc.toList("sequenceNum"));
		templateContext.put("additionalFields", passThru);
		templateContext.put("defaultValues", defaultValues);
		templateContext.put("delegator", this.delegator);
		templateContext.put("locale", Locale.getDefault());

		Template template = this.getTemplate(templateUrl);
		try {
			FreeMarkerWorker.renderTemplate(template, templateContext, writer);
		} catch (TemplateException e) {
			Debug.logError(e, "Error rendering Survey with template at [" + templateUrl.toExternalForm() + "]", module);
		} catch (IOException e) {
			Debug.logError(e, "Error rendering Survey with template at [" + templateUrl.toExternalForm() + "]", module);
		}
	}

	// returns the FTL Template object
	// Note: the template will not be cached
	protected Template getTemplate(URL templateUrl) {
		Configuration config = FreeMarkerWorker.getDefaultOfbizConfig();

		Template template = null;
		try {
			InputStream templateStream = templateUrl.openStream();
			InputStreamReader templateReader = new InputStreamReader(templateStream);
			template = new Template(templateUrl.toExternalForm(), templateReader, config);
		} catch (IOException e) {
			Debug.logError(e, "Unable to get template from URL :" + templateUrl.toExternalForm(), module);
		}
		return template;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	// returns the GenericValue object for the current Survey
	public GenericValue getSurvey() {
		GenericValue survey = null;
		try {
			survey = EntityQuery.use(delegator).from("Survey").where("surveyId", surveyId).cache().queryOne();
		} catch (GenericEntityException e) {
			Debug.logError(e, "Unable to get Survey : " + surveyId, module);
		}
		return survey;
	}

	public String getSurveyName() {
		GenericValue survey = this.getSurvey();
		if (survey != null) {
			return survey.getString("surveyName");
		}
		return "";
	}

	// true if we can update this survey
	public boolean canUpdate() {
		if (this.edit) {
			return true;
		}

		GenericValue survey = this.getSurvey();
		if (!"Y".equals(survey.getString("allowMultiple")) && !"Y".equals(survey.getString("allowUpdate"))) {
			return false;
		}
		return true;
	}

	public boolean canRespond() {
		String responseId = this.getThisResponseId();
		if (responseId == null) {
			return true;
		} else {
			GenericValue survey = this.getSurvey();
			if ("Y".equals(survey.getString("allowMultiple"))) {
				return true;
			}
		}
		return false;
	}

	// returns a list of SurveyQuestions (in order by sequence number) for the current Survey
	public List<GenericValue> getSurveyQuestionAndAppls() {
		List<GenericValue> questions = new LinkedList<GenericValue>();

		try {
			questions = EntityQuery.use(delegator).from("SurveyQuestionAndAppl")
					.where("surveyId", surveyId)
					.orderBy("sequenceNum", "surveyMultiRespColId")
					.filterByDate().cache().queryList();
		} catch (GenericEntityException e) {
			Debug.logError(e, "Unable to get questions for survey : " + surveyId, module);
		}

		return questions;
	}

	// returns the most current SurveyResponse ID for a survey; null if no party is found
	protected String getThisResponseId() {
		if (responseId != null) {
			return responseId;
		}

		if (partyId == null) {
			return null;
		}

		String responseId = null;
		List<GenericValue> responses = null;
		try {
			responses = EntityQuery.use(delegator).from("SurveyResponse")
					.where("surveyId", surveyId, "partyId", partyId)
					.orderBy("-lastModifiedDate")
					.queryList();
		} catch (GenericEntityException e) {
			Debug.logError(e, module);
		}

		if (UtilValidate.isNotEmpty(responses)) {
			GenericValue response = EntityUtil.getFirst(responses);
			responseId = response.getString("surveyResponseId");
			if (responses.size() > 1) {
				Debug.logWarning("More then one response found for survey : " + surveyId + " by party : " + partyId + " using most current", module);
			}
		}

		return responseId;
	}

	protected void setThisResponseId(String responseId) {
		this.responseId = responseId;
	}

	public long getNumberResponses() throws SurveyWrapperException {
		long responses = 0;
		try {
			responses = EntityQuery.use(delegator).from("SurveyResponse").where("surveyId", surveyId).queryCount();
		} catch (GenericEntityException e) {
			throw new SurveyWrapperException(e);
		}
		return responses;
	}

	public List<GenericValue> getSurveyResponses(GenericValue question) throws SurveyWrapperException {
		List<GenericValue> responses = null;
		try {
			responses = EntityQuery.use(delegator).from("SurveyResponse").where("surveyQuestionId", question.get("surveyQuestionId")).queryList();
		} catch (GenericEntityException e) {
			throw new SurveyWrapperException(e);
		}
		return responses;
	}

	// returns a Map of answers keyed on SurveyQuestion ID from the most current SurveyResponse ID
	public Map<String, Object> getResponseAnswers(String responseId) throws SurveyWrapperException {
		Map<String, Object> answerMap = new HashMap<String, Object>();

		if (responseId != null) {
			List<GenericValue> answers = null;
			try {
				answers = EntityQuery.use(delegator).from("SurveyResponseAnswer").where("surveyResponseId", responseId).queryList();
			} catch (GenericEntityException e) {
				Debug.logError(e, module);
			}

			if (UtilValidate.isNotEmpty(answers)) {
				for (GenericValue answer : answers) {
					answerMap.put(answer.getString("surveyQuestionId"), answer);
				}
			}
		}

		// get the pass-thru (posted form data)
		if (UtilValidate.isNotEmpty(passThru)) {
			for (String key : passThru.keySet()) {
				if (key.toUpperCase().startsWith("ANSWERS_")) {
					int splitIndex = key.indexOf('_');
					String questionId = key.substring(splitIndex + 1);
					Map<String, Object> thisAnswer = new HashMap<String, Object>();
					String answer = (String) passThru.remove(key);
					thisAnswer.put("booleanResponse", answer);
					thisAnswer.put("currencyResponse", answer);
					thisAnswer.put("floatResponse", answer);
					thisAnswer.put("numericResponse", answer);
					thisAnswer.put("textResponse", answer);
					thisAnswer.put("surveyOptionSeqId", answer);
					// this is okay since only one will be looked at
					answerMap.put(questionId, thisAnswer);
				}
			}
		}

		return answerMap;
	}

	public List<GenericValue> getQuestionResponses(GenericValue question, int startIndex, int number) throws SurveyWrapperException {
		List<GenericValue> resp = null;
		boolean beganTransaction = false;
		try {
			beganTransaction = TransactionUtil.begin();

			int maxRows = startIndex + number;
			EntityListIterator eli = this.getEli(question, maxRows);
			if (startIndex > 0 && number > 0) {
				resp = eli.getPartialList(startIndex, number);
			} else {
				resp = eli.getCompleteList();
			}

			eli.close();
		} catch (GenericEntityException e) {
			try {
				// only rollback the transaction if we started one...
				TransactionUtil.rollback(beganTransaction, "Error getting survey question responses", e);
			} catch (GenericEntityException e2) {
				Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), module);
			}

			throw new SurveyWrapperException(e);
		} finally {
			try {
				// only commit the transaction if we started one...
				TransactionUtil.commit(beganTransaction);
			} catch (GenericEntityException e) {
				throw new SurveyWrapperException(e);
			}
		}
		return resp;
	}

	public Map<String, Object> getResults(List<GenericValue> questions) throws SurveyWrapperException {
		Map<String, Object> questionResults = new HashMap<String, Object>();
		if (questions != null) {
			for (GenericValue question : questions) {
				Map<String, Object> results = getResultInfo(question);
				if (results != null) {
					questionResults.put(question.getString("surveyQuestionId"), results);
				}
			}
		}
		return questionResults;
	}

	// returns a map of question reqsults
	public Map<String, Object> getResultInfo(GenericValue question) throws SurveyWrapperException {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		// special keys in the result:
		// "_q_type"      - question type (SurveyQuestionTypeId)
		// "_a_type"      - answer type ("boolean", "option", "long", "double", "text")
		// "_total"       - number of total responses (all types)
		// "_tally"       - tally of all response values (number types)
		// "_average"     - average of all response values (number types)
		// "_yes_total"   - number of 'Y' (true) reponses (boolean type)
		// "_no_total"    - number of 'N' (false) responses (boolean type)
		// "_yes_percent" - number of 'Y' (true) reponses (boolean type)
		// "_no_percent"  - number of 'N' (false) responses (boolean type)
		// [optionId]     - Map containing '_total, _percent' keys (option type)

		String questionType = question.getString("surveyQuestionTypeId");
		resultMap.put("_q_type", questionType);

		// call the proper method based on the question type
		// note this will need to be updated as new types are added
		if ("OPTION".equals(questionType)) {
			Map<String, Object> thisResult = getOptionResult(question);
			if (thisResult != null) {
				Long questionTotal = (Long) thisResult.remove("_total");
				if (questionTotal == null) {
					questionTotal = Long.valueOf(0);
				}
				// set the total responses
				resultMap.put("_total", questionTotal);

				// create the map of option info ("_total", "_percent")
				for (String optId : thisResult.keySet()) {
					Map<String, Object> optMap = new HashMap<String, Object>();
					Long optTotal = (Long) thisResult.get(optId);
					if (optTotal == null) {
						optTotal = Long.valueOf(0);
					}
					Long percent = Long.valueOf((long) (((double) optTotal.longValue() / (double) questionTotal.longValue()) * 100));
					optMap.put("_total", optTotal);
					optMap.put("_percent", percent);
					resultMap.put(optId, optMap);
				}
				resultMap.put("_a_type", "option");
			}
		} else if ("BOOLEAN".equals(questionType)) {
			long[] thisResult = getBooleanResult(question);
			long yesPercent = thisResult[1] > 0 ? (long) (((double) thisResult[1] / (double) thisResult[0]) * 100) : 0;
			long noPercent = thisResult[2] > 0 ? (long) (((double) thisResult[2] / (double) thisResult[0]) * 100) : 0;

			resultMap.put("_total", Long.valueOf(thisResult[0]));
			resultMap.put("_yes_total", Long.valueOf(thisResult[1]));
			resultMap.put("_no_total", Long.valueOf(thisResult[2]));
			resultMap.put("_yes_percent", Long.valueOf(yesPercent));
			resultMap.put("_no_percent", Long.valueOf(noPercent));
			resultMap.put("_a_type", "boolean");
		} else if ("NUMBER_LONG".equals(questionType)) {
			double[] thisResult = getNumberResult(question, 1);
			resultMap.put("_total", Long.valueOf((long) thisResult[0]));
			resultMap.put("_tally", Long.valueOf((long) thisResult[1]));
			resultMap.put("_average", Long.valueOf((long) thisResult[2]));
			resultMap.put("_a_type", "long");
		} else if ("NUMBER_CURRENCY".equals(questionType)) {
			double[] thisResult = getNumberResult(question, 2);
			resultMap.put("_total", Long.valueOf((long) thisResult[0]));
			resultMap.put("_tally", Double.valueOf(thisResult[1]));
			resultMap.put("_average", Double.valueOf(thisResult[2]));
			resultMap.put("_a_type", "double");
		} else if ("NUMBER_FLOAT".equals(questionType)) {
			double[] thisResult = getNumberResult(question, 3);
			resultMap.put("_total", Long.valueOf((long) thisResult[0]));
			resultMap.put("_tally", Double.valueOf(thisResult[1]));
			resultMap.put("_average", Double.valueOf(thisResult[2]));
			resultMap.put("_a_type", "double");
		} else if ("SEPERATOR_LINE".equals(questionType) || "SEPERATOR_TEXT".equals(questionType)) {
			// not really a question; ingore completely
			return null;
		} else {
			// default is text
			resultMap.put("_total", Long.valueOf(getTextResult(question)));
			resultMap.put("_a_type", "text");
		}

		return resultMap;
	}

	private long[] getBooleanResult(GenericValue question) throws SurveyWrapperException {
		boolean beganTransaction = false;
		try {
			beganTransaction = TransactionUtil.begin();

			long[] result = {0, 0, 0};
			// index 0 = total responses
			// index 1 = total yes
			// index 2 = total no

			EntityListIterator eli = this.getEli(question, -1);

			if (eli != null) {
				GenericValue value;
				while (((value = eli.next()) != null)) {
					if ("Y".equalsIgnoreCase(value.getString("booleanResponse"))) {
						result[1]++;
					} else {
						result[2]++;
					}
					result[0]++; // increment the count
				}

				eli.close();
			}

			return result;
		} catch (GenericEntityException e) {
			try {
				// only rollback the transaction if we started one...
				TransactionUtil.rollback(beganTransaction, "Error getting survey question responses Boolean result", e);
			} catch (GenericEntityException e2) {
				Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), module);
			}

			throw new SurveyWrapperException(e);
		} finally {
			try {
				// only commit the transaction if we started one...
				TransactionUtil.commit(beganTransaction);
			} catch (GenericEntityException e) {
				throw new SurveyWrapperException(e);
			}
		}
	}

	private double[] getNumberResult(GenericValue question, int type) throws SurveyWrapperException {
		double[] result = {0, 0, 0};
		// index 0 = total responses
		// index 1 = tally
		// index 2 = average

		boolean beganTransaction = false;
		try {
			beganTransaction = TransactionUtil.begin();

			EntityListIterator eli = this.getEli(question, -1);

			if (eli != null) {
				GenericValue value;
				while (((value = eli.next()) != null)) {
					switch (type) {
						case 1:
							Long n = value.getLong("numericResponse");
							if (UtilValidate.isNotEmpty(n)) {
								result[1] += n.longValue();
							}
							break;
						case 2:
							Double c = value.getDouble("currencyResponse");
							if (UtilValidate.isNotEmpty(c)) {
								result[1] += (((double) Math.round((c.doubleValue() - c.doubleValue()) * 100)) / 100);
							}
							break;
						case 3:
							Double f = value.getDouble("floatResponse");
							if (UtilValidate.isNotEmpty(f)) {
								result[1] += f.doubleValue();
							}
							break;
					}
					result[0]++; // increment the count
				}

				eli.close();
			}
		} catch (GenericEntityException e) {
			try {
				// only rollback the transaction if we started one...
				TransactionUtil.rollback(beganTransaction, "Error getting survey question responses Number result", e);
			} catch (GenericEntityException e2) {
				Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), module);
			}

			throw new SurveyWrapperException(e);
		} finally {
			try {
				// only commit the transaction if we started one...
				TransactionUtil.commit(beganTransaction);
			} catch (GenericEntityException e) {
				throw new SurveyWrapperException(e);
			}
		}

		// average
		switch (type) {
			case 1:
				if (result[0] > 0)
					result[2] = ((long) result[1]) / ((long) result[0]);
				break;
			case 2:
				if (result[0] > 0)
					result[2] = (((double) Math.round((result[1] / result[0]) * 100)) / 100);
				break;
			case 3:
				if (result[0] > 0)
					result[2] = result[1] / result[0];
				break;
		}

		return result;
	}

	private long getTextResult(GenericValue question) throws SurveyWrapperException {
		long result = 0;

		try {
			result = EntityQuery.use(delegator).from("SurveyResponseAndAnswer")
					.where(makeEliCondition(question))
					.queryCount();
		} catch (GenericEntityException e) {
			Debug.logError(e, module);
			throw new SurveyWrapperException("Unable to get responses", e);
		}

		return result;
	}

	private Map<String, Object> getOptionResult(GenericValue question) throws SurveyWrapperException {
		Map<String, Object> result = new HashMap<String, Object>();
		long total = 0;

		boolean beganTransaction = false;
		try {
			beganTransaction = TransactionUtil.begin();

			EntityListIterator eli = this.getEli(question, -1);
			if (eli != null) {
				GenericValue value;
				while (((value = eli.next()) != null)) {
					String optionId = value.getString("surveyOptionSeqId");
					if (UtilValidate.isNotEmpty(optionId)) {
						Long optCount = (Long) result.remove(optionId);
						if (optCount == null) {
							optCount = Long.valueOf(1);
						} else {
							optCount = Long.valueOf(1 + optCount.longValue());
						}
						result.put(optionId, optCount);
						total++; // increment the count
					}
				}

				eli.close();
			}
		} catch (GenericEntityException e) {
			try {
				// only rollback the transaction if we started one...
				TransactionUtil.rollback(beganTransaction, "Error getting survey question responses Option result", e);
			} catch (GenericEntityException e2) {
				Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), module);
			}

			throw new SurveyWrapperException(e);
		} finally {
			try {
				// only commit the transaction if we started one...
				TransactionUtil.commit(beganTransaction);
			} catch (GenericEntityException e) {
				throw new SurveyWrapperException(e);
			}
		}

		result.put("_total", Long.valueOf(total));
		return result;
	}

	private EntityCondition makeEliCondition(GenericValue question) {
		return EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("surveyQuestionId",
				EntityOperator.EQUALS, question.getString("surveyQuestionId")),
				EntityCondition.makeCondition("surveyId", EntityOperator.EQUALS, surveyId)), EntityOperator.AND);
	}

	private EntityListIterator getEli(GenericValue question, int maxRows) throws GenericEntityException {
		return EntityQuery.use(delegator).from("SurveyResponseAndAnswer")
				.where(makeEliCondition(question))
				.cursorScrollInsensitive()
				.maxRows(maxRows)
				.queryIterator();
	}

	@SuppressWarnings("serial")
	protected class SurveyWrapperException extends GeneralException {

		public SurveyWrapperException() {
			super();
		}

		public SurveyWrapperException(String str) {
			super(str);
		}

		public SurveyWrapperException(String str, Throwable nested) {
			super(str, nested);
		}

		public SurveyWrapperException(Throwable nested) {
			super(nested);
		}
	}
}
