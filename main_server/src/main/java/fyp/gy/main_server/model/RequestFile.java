package fyp.gy.main_server.model;

import org.springframework.core.io.InputStreamResource;

public class RequestFile {

    private String filename;
    private InputStreamResource resource;

    public String getFilename() {
        return filename;
    }
    public InputStreamResource getResource() {
        return resource;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    public void setResource(InputStreamResource resource) {
        this.resource = resource;
    }
}
