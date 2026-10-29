import java.util.ArrayList;

public class SistemaUsuarios {

    // Lista privada de usuarios
    private ArrayList<Usuario> lista;

    public SistemaUsuarios() {
        this.lista = new ArrayList<Usuario>();
    }

    // CREATE: agrega un usuario nuevo
    public void registrar(Usuario usuario) {
        lista.add(usuario);
    }

    // READ: devuelve el usuario si existe, si no devuelve null
    public Usuario buscar(String nombreUsuario) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).obtenerUsuario().equals(nombreUsuario)) {
                return lista.get(i);
            }
        }
        return null;
    }

    // UPDATE: se hace con usuario.cambiarClave(...) (clase Usuario)

    // DELETE: elimina un usuario
    public void eliminar(Usuario usuario) {
        lista.remove(usuario);
    }

    // Imprime en la terminal todos los usuarios guardados (para mostrar en la sustentacion)
    public void imprimir() {
        System.out.println();
        System.out.println("===== USUARIOS GUARDADOS EN JAVA (" + lista.size() + ") =====");
        for (int i = 0; i < lista.size(); i++) {
            Usuario u = lista.get(i);
            System.out.println((i + 1) + ". Nombre: " + u.obtenerNombre()
                    + " | Usuario: " + u.obtenerUsuario()
                    + " | Clave: " + "*".repeat(u.obtenerClave().length()));
        }
        System.out.println("=========================================");
    }
}
