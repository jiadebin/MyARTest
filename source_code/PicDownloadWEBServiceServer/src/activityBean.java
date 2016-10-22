
public class activityBean {

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getPublishtime() {
		return publishtime;
	}
	public void setPublishtime(String publishtime) {
		this.publishtime = publishtime;
	}
	private int id;
	private String title;
	private String content;
	private String publishtime;
	public activityBean(int id, String title, String content, String publishtime) {
		super();
		this.id = id;
		this.title = title;
		this.content = content;
		this.publishtime = publishtime;
	}
	
}
