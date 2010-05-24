/*
 * Copyright 2009 - 2010 Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of the SubmissionInterface.
 * 
 * SubmissionInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * 
 * SubmissionInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SubmissionInterface. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.template;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * An easy template
 * @author Sven Strickroth
 */
public abstract class Template {
	protected HttpServletResponse servletResponse;
	protected RequestAdapter requestAdapter;
	protected String prefix;
	private List<String> headers = new LinkedList<String>();

	public Template(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
		this.servletResponse = servletResponse;
		prefix = servletRequest.getContextPath();
		requestAdapter = new RequestAdapter(servletRequest);
	}

	/**
	 * print a HTML page heading
	 * @param title
	 * @throws IOException
	 */
	public void printTemplateHeader(String title) throws IOException {
		printTemplateHeader(title, "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; " + title);
	}

	public void printTemplateHeader(Lecture lecture) throws IOException {
		printTemplateHeader("Veranstaltung \"" + Util.mknohtml(lecture.getName()) + "\"", "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; Veranstaltung \"" + Util.mknohtml(lecture.getName()) + "\"");
	}

	public void printTemplateHeader(String title, Lecture lecture) throws IOException {
		printTemplateHeader(title, "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowLecture?lecture=" + lecture.getId()) + "\">Veranstaltung \"" + Util.mknohtml(lecture.getName()) + "\"</a> &gt; " + title);
	}

	public void printTemplateHeader(Task task) throws IOException {
		printTemplateHeader("Aufgabe \"" + Util.mknohtml(task.getTitle()) + "\"", "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowLecture?lecture=" + task.getLecture().getId()) + "\">Veranstaltung \"" + Util.mknohtml(task.getLecture().getName()) + "\"</a> &gt; " + "Aufgabe \"" + Util.mknohtml(task.getTitle()) + "\"");
	}

	public void printTemplateHeader(String title, Task task) throws IOException {
		printTemplateHeader(title, "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowLecture?lecture=" + task.getLecture().getId()) + "\">Veranstaltung \"" + Util.mknohtml(task.getLecture().getName()) + "\"</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowTask?taskid=" + task.getTaskid()) + "\">Aufgabe \"" + Util.mknohtml(task.getTitle()) + "\"</a> &gt; " + title);
	}

	public void printTemplateHeader(Submission submission) throws IOException {
		printTemplateHeader("Abgabe von \"" + Util.mknohtml(submission.getSubmitterNames()) + "\"", "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowLecture?lecture=" + submission.getTask().getLecture().getId()) + "\">Veranstaltung \"" + Util.mknohtml(submission.getTask().getLecture().getName()) + "\"</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowTask?taskid=" + submission.getTask().getTaskid()) + "\">Aufgabe \"" + Util.mknohtml(submission.getTask().getTitle()) + "\"</a> &gt; Abgabe von \"" + Util.mknohtml(submission.getSubmitterNames()) + "\"");
	}

	public void printTemplateHeader(String title, Submission submission) throws IOException {
		printTemplateHeader("Abgabe von \"" + Util.mknohtml(submission.getSubmitterNames()) + "\"", "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowLecture?lecture=" + submission.getTask().getLecture().getId()) + "\">Veranstaltung \"" + Util.mknohtml(submission.getTask().getLecture().getName()) + "\"</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowTask?taskid=" + submission.getTask().getTaskid()) + "\">Aufgabe \"" + Util.mknohtml(submission.getTask().getTitle()) + "\"</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowSubmission?sid=" + submission.getSubmissionid()) + "\">Abgabe von \"" + Util.mknohtml(submission.getSubmitterNames()) + "\"</a> &gt; " + title);
	}

	public void printTemplateHeader(Group group) throws IOException {
		printTemplateHeader("Gruppe \"" + Util.mknohtml(group.getName()) + "\"", "<a href=\"" + servletResponse.encodeURL("Overview") + "\">Meine Veranstaltungen</a> &gt; <a href=\"" + servletResponse.encodeURL("ShowLecture?lecture=" + group.getLecture().getId()) + "\">Veranstaltung \"" + Util.mknohtml(group.getLecture().getName()) + "\"</a> &gt; Gruppe \"" + Util.mknohtml(group.getName()) + "\"");
	}

	public void addHead(String header) {
		headers.add(header);
	}

	protected void getHeads() throws IOException {
		for (String header : headers) {
			servletResponse.getWriter().println(header + "\n");
		}
	}

	/**
	 * Prints the HTML page header with a title and breadcrums
	 * @param title
	 * @param breadCrum
	 * @throws IOException
	 */
	public abstract void printTemplateHeader(String title, String breadCrum) throws IOException;

	/**
	 * print a HTML page footer
	 * @throws IOException
	 */
	public abstract void printTemplateFooter() throws IOException;
}
