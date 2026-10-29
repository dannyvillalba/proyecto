import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class Servidor {

    // Los dos objetos principales del backend
    private static SistemaUsuarios sistema = new SistemaUsuarios();
    private static Calculadora calculadora = new Calculadora();

    private static final String ERROR_NUMEROS = "ERROR\nEscribe todos los datos con numeros mayores a 0";

    public static void main(String[] args) throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(8081), 0);

        // Las rutas que empiezan con /api/ las atiende el backend
        servidor.createContext("/api/", exchange -> atenderApi(exchange));
        // Todo lo demas (index.html, css, js) se entrega como archivo
        servidor.createContext("/", exchange -> servirArchivo(exchange));

        servidor.start();
        System.out.println("Servidor listo. Abre en el navegador: http://localhost:8081");
    }

    // ---------- Decide que hacer segun la ruta ----------
    private static void atenderApi(HttpExchange exchange) throws IOException {
        String ruta = exchange.getRequestURI().getPath().replace("/api/", "");
        Map<String, String> d = leerDatos(exchange);
        String respuesta;

        if (ruta.equals("registro")) {
            respuesta = registro(d);
        } else if (ruta.equals("login")) {
            respuesta = login(d);
        } else if (ruta.equals("recuperar")) {
            respuesta = recuperar(d);
        } else if (ruta.equals("eliminar")) {
            respuesta = eliminar(d);
        } else if (ruta.equals("muro")) {
            respuesta = muro(d);
        } else if (ruta.equals("viga")) {
            respuesta = viga(d);
        } else if (ruta.equals("columnas")) {
            respuesta = columnas(d);
        } else if (ruta.equals("contrapiso")) {
            respuesta = contrapiso(d);
        } else if (ruta.equals("techo")) {
            respuesta = techo(d);
        } else if (ruta.equals("pisos")) {
            respuesta = pisos(d);
        } else if (ruta.equals("pintura")) {
            respuesta = pintura(d);
        } else {
            respuesta = "ERROR\nEsa ruta no existe";
        }

        System.out.println(">> Ruta: /api/" + ruta + "\n" + respuesta);
        enviar(exchange, respuesta);
    }

    // ---------- USUARIOS (CRUD) ----------

    // CREATE
    private static String registro(Map<String, String> d) {
        String nombre = d.getOrDefault("nombre", "").trim();
        String usuario = d.getOrDefault("usuario", "").trim();
        String clave = d.getOrDefault("clave", "");
        String clave2 = d.getOrDefault("clave2", "");

        if (nombre.equals("") || usuario.equals("") || clave.equals("")) {
            return "ERROR\nCompleta todos los campos";
        }
        if (clave.length() < 6) {
            return "ERROR\nLa contrasena debe tener minimo 6 caracteres";
        }
        if (!clave.equals(clave2)) {
            return "ERROR\nLas contrasenas no coinciden";
        }
        if (sistema.buscar(usuario) != null) {
            return "ERROR\nEse usuario ya existe, elige otro";
        }

        Usuario nuevo = new Usuario(nombre, usuario, clave); // aqui se usa el constructor
        sistema.registrar(nuevo);
        sistema.imprimir(); // muestra los usuarios en la terminal
        return "OK\nCuenta creada. Ya puedes iniciar sesion";
    }

    // READ
    private static String login(Map<String, String> d) {
        String usuario = d.getOrDefault("usuario", "").trim();
        String clave = d.getOrDefault("clave", "");

        Usuario encontrado = sistema.buscar(usuario);

        if (encontrado == null || !encontrado.claveCorrecta(clave)) {
            return "ERROR\nUsuario o contrasena incorrectos";
        }
        return "OK\n" + encontrado.obtenerNombre();
    }

    // UPDATE
    private static String recuperar(Map<String, String> d) {
        String usuario = d.getOrDefault("usuario", "").trim();
        String nombre = d.getOrDefault("nombre", "").trim();
        String claveNueva = d.getOrDefault("clave", "");

        Usuario encontrado = sistema.buscar(usuario);

        if (encontrado == null) {
            return "ERROR\nEse usuario no existe";
        }
        if (!encontrado.obtenerNombre().equalsIgnoreCase(nombre)) {
            return "ERROR\nEl nombre completo no coincide con el usuario";
        }
        if (claveNueva.length() < 6) {
            return "ERROR\nLa contrasena debe tener minimo 6 caracteres";
        }

        encontrado.cambiarClave(claveNueva);
        sistema.imprimir(); // muestra los usuarios en la terminal
        return "OK\nContrasena cambiada. Ya puedes iniciar sesion";
    }

    // DELETE
    private static String eliminar(Map<String, String> d) {
        String usuario = d.getOrDefault("usuario", "").trim();
        String clave = d.getOrDefault("clave", "");

        Usuario encontrado = sistema.buscar(usuario);

        if (encontrado == null || !encontrado.claveCorrecta(clave)) {
            return "ERROR\nUsuario o contrasena incorrectos";
        }

        sistema.eliminar(encontrado);
        sistema.imprimir(); // muestra los usuarios en la terminal
        return "OK\nCuenta eliminada";
    }

    // ---------- CALCULOS ----------

    private static String muro(Map<String, String> d) {
        double largo = numero(d, "largo");
        double alto = numero(d, "alto");
        if (largo <= 0 || alto <= 0) {
            return ERROR_NUMEROS;
        }

        if (numero(d, "espesor") == 20) {
            calculadora.cambiarEspesor(20);
        } else {
            calculadora.cambiarEspesor(30);
        }
        return "OK\n" + calculadora.calcularMuro(largo, alto);
    }

    private static String viga(Map<String, String> d) {
        double largo = numero(d, "largo");
        double ancho = numero(d, "ancho");
        double alto = numero(d, "alto");
        double cantidad = numero(d, "cantidad");
        if (largo <= 0 || ancho <= 0 || alto <= 0 || cantidad <= 0) {
            return ERROR_NUMEROS;
        }
        return "OK\n" + calculadora.calcularViga(largo, ancho, alto, cantidad);
    }

    private static String columnas(Map<String, String> d) {
        double cantidad = numero(d, "cantidad");
        double alto = numero(d, "alto");
        double lado1 = numero(d, "lado1");
        double lado2 = numero(d, "lado2");
        if (cantidad <= 0 || alto <= 0 || lado1 <= 0 || lado2 <= 0) {
            return ERROR_NUMEROS;
        }
        return "OK\n" + calculadora.calcularColumnas(cantidad, alto, lado1, lado2);
    }

    private static String contrapiso(Map<String, String> d) {
        double largo = numero(d, "largo");
        double ancho = numero(d, "ancho");
        double espesor = numero(d, "espesor");
        if (largo <= 0 || ancho <= 0 || espesor <= 0) {
            return ERROR_NUMEROS;
        }
        return "OK\n" + calculadora.calcularContrapiso(largo, ancho, espesor);
    }

    private static String techo(Map<String, String> d) {
        double largo = numero(d, "largo");
        double ancho = numero(d, "ancho");
        if (largo <= 0 || ancho <= 0) {
            return ERROR_NUMEROS;
        }
        return "OK\n" + calculadora.calcularTecho(largo, ancho);
    }

    private static String pisos(Map<String, String> d) {
        double largo = numero(d, "largo");
        double ancho = numero(d, "ancho");
        double lado = numero(d, "lado");
        if (largo <= 0 || ancho <= 0 || lado <= 0) {
            return ERROR_NUMEROS;
        }
        return "OK\n" + calculadora.calcularPisos(largo, ancho, lado);
    }

    private static String pintura(Map<String, String> d) {
        double largo = numero(d, "largo");
        double alto = numero(d, "alto");
        double manos = numero(d, "manos");
        if (largo <= 0 || alto <= 0 || manos <= 0) {
            return ERROR_NUMEROS;
        }
        return "OK\n" + calculadora.calcularPintura(largo, alto, manos);
    }

    // ---------- FUNCIONES DE APOYO ----------

    // Convierte un dato en numero. Si falla devuelve -1
    private static double numero(Map<String, String> d, String clave) {
        try {
            return Double.parseDouble(d.get(clave));
        } catch (Exception e) {
            return -1;
        }
    }

    // Lee los datos que manda el JS (formato: nombre=Juan&usuario=juan1)
    private static Map<String, String> leerDatos(HttpExchange exchange) throws IOException {
        String cuerpo = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        Map<String, String> datos = new HashMap<String, String>();

        if (cuerpo.isEmpty()) {
            return datos;
        }

        String[] pares = cuerpo.split("&");
        for (int i = 0; i < pares.length; i++) {
            String[] partes = pares[i].split("=", 2);
            String clave = URLDecoder.decode(partes[0], "UTF-8");
            String valor = "";
            if (partes.length > 1) {
                valor = URLDecoder.decode(partes[1], "UTF-8");
            }
            datos.put(clave, valor);
        }
        return datos;
    }

    // Envia una respuesta de texto al navegador
    private static void enviar(HttpExchange exchange, String texto) throws IOException {
        byte[] bytes = texto.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    // Entrega index.html, style.css y script.js al navegador
    private static void servirArchivo(HttpExchange exchange) throws IOException {
        String ruta = exchange.getRequestURI().getPath();
        if (ruta.equals("/")) {
            ruta = "/index.html";
        }

        // Solo se entregan archivos html, css y js
        String tipo = null;
        if (ruta.endsWith(".html")) {
            tipo = "text/html";
        } else if (ruta.endsWith(".css")) {
            tipo = "text/css";
        } else if (ruta.endsWith(".js")) {
            tipo = "text/javascript";
        }

        File archivo = new File("." + ruta);

        if (tipo == null || ruta.contains("..") || !archivo.isFile()) {
            byte[] mensaje = "No encontrado".getBytes("UTF-8");
            exchange.sendResponseHeaders(404, mensaje.length);
            exchange.getResponseBody().write(mensaje);
            exchange.close();
            return;
        }

        byte[] contenido = Files.readAllBytes(archivo.toPath());
        exchange.getResponseHeaders().set("Content-Type", tipo + "; charset=UTF-8");
        exchange.sendResponseHeaders(200, contenido.length);
        exchange.getResponseBody().write(contenido);
        exchange.close();
    }
}
