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

package de.tuclausthal.submissioninterface.servlets.view;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a submission to a tutor
 * @author Sven Strickroth
 */
public class ShowSubmissionView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Session session = HibernateSessionHelper.getSessionFactory().openSession();

		Submission submission = (Submission) request.getAttribute("submission");

		List<String> submittedFiles = (List<String>) request.getAttribute("submittedFiles");
		Task task = submission.getTask();

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(new SessionAdapter(request).getUser(session), submission.getTask().getLecture());

		template.printTemplateHeader(submission);

		if (submission.getSubmitters().iterator().next().getGroup() != null) {
			out.println("<h2>Gruppe: " + submission.getSubmitters().iterator().next().getGroup().getName() + "</h2>");
		}

		if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
			out.println("<h2>Bewertung:</h2>");
			out.println("<table class=border>");
			String oldComment = "";
			int points = 0;
			boolean pointsOk = false;
			String pointsGivenBy = "";
			if (submission.getPoints() != null) {
				oldComment = submission.getPoints().getComment();
				points = submission.getPoints().getPoints();
				pointsOk = submission.getPoints().getPointsOk();
				pointsGivenBy = " (bisher " + points + " Punkte  vergeben von: " + submission.getPoints().getIssuedBy().getUser().getFullName() + ")";
			}
			out.println("<tr>");
			out.println("<td>");
			out.println("<form action=\"?\" method=post>");
			out.println("<input type=hidden name=sid value=\"" + submission.getSubmissionid() + "\">");
			out.println("<b>Punkte:</b> <input type=text name=points size=3 value=\"" + points + "\"> (max. " + task.getMaxPoints() + ")" + pointsGivenBy + "<br>");
			out.println("<b>Kommentar:</b><br><textarea cols=80 rows=8 name=comment>" + Util.mknohtml(oldComment) + "</textarea><br>");
			out.println("<b>Abgenommen:</b> <input type=checkbox name=pointsok " + (pointsOk ? "checked" : "") + "><br>");
			out.println("<input type=submit value=Speichern>");
			out.println("</form>");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table>");

			out.println("<p>");
		}

		if (submission.getSimilarSubmissions().size() > 0) {
			out.println("<h2>Ähnliche Abgaben:</h2>");
			out.println("<table>");
			out.println("<tr>");
			for (SimilarityTest similarityTest : task.getSimularityTests()) {
				out.println("<th><span title=\"Ähnlichkeit zu\">" + similarityTest + "</span></th>");
			}
			out.println("</tr>");
			out.println("<tr>");
			for (SimilarityTest similarityTest : task.getSimularityTests()) {
				out.println("<td>");
				out.println("<table class=border>");
				out.println("<tr>");
				out.println("<th>Student</th>");
				out.println("<th>Ähnlichkeit</th>");
				out.println("</tr>");
				for (Similarity similarity : DAOFactory.SimilarityDAOIf(session).getUsersWithSimilarity(similarityTest, submission)) {
					out.println("<tr>");
					out.println("<td><a href=\"" + response.encodeURL("ShowSubmission?sid=" + similarity.getSubmissionTwo().getSubmissionid()) + "\">" + Util.mknohtml(similarity.getSubmissionTwo().getSubmitterNames()) + "</a></td>");
					out.println("<td class=points>" + similarity.getPercentage() + "%</td>");
					out.println("</tr>");
				}
				out.println("</table>");
				out.println("</td>");
			}
			out.println("</tr>");
			out.println("</table><p>");
		}

		if (submission.getTestResults().size() > 0) {
			out.println("<h2>Tests:</h2>");
			out.println("<ul>");
			for (TestResult testResult : submission.getTestResults()) {
				out.println("<li>" + testResult.getTest().getTestTitle() + "<br>");
				out.println("Erfolgreich: " + Util.boolToHTML(testResult.getPassedTest()));
				if (!testResult.getTestOutput().isEmpty()) {
					out.println("<br><textarea cols=80 rows=15>" + Util.mknohtml(testResult.getTestOutput()) + "</textarea>");
				}
				out.println("</li>");
			}
			out.println("</ul>");
		}

		out.println("<h2>Dateien:</h2>");
		out.println("<div class=mid>");
		out.println("<p><a href=\"DownloadAsZip?sid=" + submission.getSubmissionid() + "\">alles als .zip herunterladen</a></p>");
		for (String file : submittedFiles) {
			file = file.replace(System.getProperty("file.separator"), "/");
			if (file.endsWith(".txt") || file.endsWith(".java") || file.endsWith(".pdf") || file.endsWith(".jpg") || file.endsWith(".png") || file.endsWith(".gif")) {
				out.println("<h3>" + Util.mknohtml(file) + "</h3>");
				out.println("<iframe width=800 height=600 src=\"" + response.encodeURL("ShowFile/" + file + "?sid=" + submission.getSubmissionid()) + "\"></iframe><br>");
				if (file.endsWith(".java")) {
					out.println("<a href=\"" + response.encodeURL("ShowFile/" + file + "?sid=" + submission.getSubmissionid()) + "&comments=off\" target=\"_blank\">" + Util.mknohtml(file) + " ohne Kommentare anzeigen</a><br>");
				}
			}
			out.println("<a href=\"" + response.encodeURL("ShowFile/" + file + "?download=true&sid=" + submission.getSubmissionid()) + "\">Download " + Util.mknohtml(file) + "</a><p>");
		}
		out.println("</div>");
		template.printTemplateFooter();
	}
}
