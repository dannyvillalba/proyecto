public class Usuario {

    // Atributos privados
    private String nombre;
    private String usuario;
    private String clave;

    // Constructor
    public Usuario(String nombre, String usuario, String clave) {
        this.nombre = nombre;
        this.usuario = usuario;
        this.clave = clave;
    }

    // Metodos para leer los atributos privados
    public String obtenerNombre() {
        return nombre;
    }

    public String obtenerUsuario() {
        return usuario;
    }

    // Devuelve la contrasena (solo se usa para mostrarla en la terminal en la sustentacion)
    public String obtenerClave() {
        return clave;
    }

    // Revisa si la contrasena es la correcta
    public boolean claveCorrecta(String claveEscrita) {
        return clave.equals(claveEscrita);
    }

    // Cambia la contrasena
    public void cambiarClave(String claveNueva) {
        this.clave = claveNueva;
    }
}
