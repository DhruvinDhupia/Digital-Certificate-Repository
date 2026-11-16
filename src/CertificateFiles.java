import java.nio.file.Path;
import java.nio.file.Paths;

public final class CertificateFiles {
    private CertificateFiles() {}
    public static Path resolve(String storedPath) {
        Path p = Paths.get(storedPath == null ? "" : storedPath);
        if (p.isAbsolute()) return p.normalize();
        return Paths.get(System.getProperty("user.dir")).resolve(p).normalize();
    }
}
