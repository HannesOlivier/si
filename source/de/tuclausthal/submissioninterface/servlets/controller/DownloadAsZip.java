/*
 * Copyright 2010 Sven Strickroth <email@cs-ware.de>
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

package de.tuclausthal.submissioninterface.servlets.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.util.ContextAdapter;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * Controller-Servlet for displaying the source of a file of a submission
 * @author Sven Strickroth
 *
 */
public class DownloadAsZip extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Session session = HibernateSessionHelper.getSessionFactory().openSession();
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);
		Submission submission = submissionDAO.getSubmission(Util.parseInteger(request.getParameter("sid"), 0));

		ContextAdapter contextAdapter = new ContextAdapter(getServletContext());

		if (submission == null) {
			request.setAttribute("title", "Abgabe nicht gefunden");
			request.getRequestDispatcher("/" + contextAdapter.getServletsPath() + "/MessageView").forward(request, response);
			return;
		}

		Task task = submission.getTask();

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		Participation participation = participationDAO.getParticipation(new SessionAdapter(request).getUser(session), submission.getTask().getLecture());
		if (participation == null || (participation.getRoleType().compareTo(ParticipationRole.TUTOR) < 0 && !submission.getSubmitters().contains(participation))) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "insufficient rights");
			return;
		}

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=submission-id" + submission.getSubmissionid() + ".zip");

		File path = new File(contextAdapter.getDataPath().getAbsolutePath() + System.getProperty("file.separator") + task.getLecture().getId() + System.getProperty("file.separator") + task.getTaskid() + System.getProperty("file.separator") + submission.getSubmissionid() + System.getProperty("file.separator"));
		ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
		//out.setMethod(ZipOutputStream.STORED);
		System.out.println(path.toString() + " " + path.exists());
		if (path.exists()) {
			recursivelyZip(out, path, "");
		}
		out.close();
	}

	private void recursivelyZip(ZipOutputStream out, File path, String relativePath) {
		for (File file : path.listFiles()) {
			try {
				if (file.isFile()) {
					out.putNextEntry(new ZipEntry(relativePath + file.getName()));
					int read = 0;
					byte[] data = new byte[1024];

					FileInputStream in = new FileInputStream(file);

					while ((read = in.read(data, 0, 1024)) != -1) {
						out.write(data, 0, read);
					}
					out.closeEntry();
					in.close();
				} else {
					recursivelyZip(out, file, relativePath + file.getName() + System.getProperty("file.separator"));
				}
			} catch (Exception e) {
				// ignore ;)
				System.out.println("Err: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}