package mx.iap.iw.services.notificationservice.datatransfer;

public class SystemResponseDTO {
	private String description;
	private Integer code;
	
	public SystemResponseDTO() {
	}
	
	public SystemResponseDTO(String description, Integer code) {
		this.description = description;
		this.code = code;
	}
	
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getCode() {
		return this.code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "SystemResponseDTO [description=" + description + ", code="
				+ code + "]";
	}
}
