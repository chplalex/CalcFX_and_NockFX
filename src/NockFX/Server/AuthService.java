package NockFX.Server;

public interface AuthService {
    public abstract String getNickByLogAndPass(String log, String pass);
}
