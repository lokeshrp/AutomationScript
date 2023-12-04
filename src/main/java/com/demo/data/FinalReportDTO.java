package com.demo.data;

import java.util.List;

public class FinalReportDTO {
	List<ReportDTO> reportDTOs;
	private List<ReportDTO> errorReportDTOs;

	public List<ReportDTO> getReportDTOs() {
		return reportDTOs;
	}

	public void setReportDTOs(List<ReportDTO> reportDTOs) {
		this.reportDTOs = reportDTOs;
	}

	public List<ReportDTO> getErrorReportDTOs() {
		return errorReportDTOs;
	}

	public void setErrorReportDTOs(List<ReportDTO> errorReportDTOs) {
		this.errorReportDTOs = errorReportDTOs;
	}

}
