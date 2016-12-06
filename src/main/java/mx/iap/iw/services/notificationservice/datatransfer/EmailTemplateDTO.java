package mx.iap.iw.services.notificationservice.datatransfer;

public class EmailTemplateDTO {
	private String body;
	private String cc;
	private String to;
	private String name;
	
	public EmailTemplateDTO() {
	}

	public EmailTemplateDTO(String body, String cc, String to, String name) {
		super();
		this.body = body;
		this.cc = cc;
		this.to = to;
		this.name = name;
	}


	public String getBody() {
		return body;
	}


	public void setBody(String body) {
		this.body = body;
	}


	public String getCc() {
		return cc;
	}


	public void setCc(String cc) {
		this.cc = cc;
	}


	public String getTo() {
		return to;
	}


	public void setTo(String to) {
		this.to = to;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "EmailTemplateDTO [to=" + to + ", cc="
				+ cc + ", body="
						+ body + ", name="
								+ name +"]";
	}
}
