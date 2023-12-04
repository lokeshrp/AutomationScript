package com.demo.data;

public class ReportDTO {

	private String PmkId;
	private String name;
	private String FidNumber;
	private String GoiRegNo;
	private boolean isSucess;
	public String getPmkId() {
		return PmkId;
	}
	public void setPmkId(String pmkId) {
		PmkId = pmkId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFidNumber() {
		return FidNumber;
	}
	public void setFidNumber(String fidNumber) {
		FidNumber = fidNumber;
	}
	public String getGoiRegNo() {
		return GoiRegNo;
	}
	public void setGoiRegNo(String goiRegNo) {
		GoiRegNo = goiRegNo;
	}
	public boolean isSucess() {
		return isSucess;
	}
	public void setSucess(boolean isSucess) {
		this.isSucess = isSucess;
	}
	@Override
	public String toString() {
		return "ReportDTO [PmkId=" + PmkId + ", name=" + name + ", FidNumber=" + FidNumber + ", GoiRegNo=" + GoiRegNo
				+ ", isSucess=" + isSucess + "]";
	}
	
	
}
