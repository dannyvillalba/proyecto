// =====================================================
//  CALCULADORA DE MATERIALES - script.js
//  Ahora TODO pasa por el backend en Java (rutas /api/...)
//  El JS solo muestra pantallas, lee los inputs y pinta resultados
// =====================================================

// Variable para saber que espesor de muro esta elegido (20 o 30)
var espesorMuro = 30;


// ---------- FUNCIONES GENERALES ----------

// Muestra una pantalla y esconde las demas
function mostrarPantalla(id) {
  var pantallas = document.querySelectorAll(".pantalla");
  for (var i = 0; i < pantallas.length; i++) {
    pantallas[i].classList.add("oculta");
  }
  document.getElementById(id).classList.remove("oculta");
  window.scrollTo(0, 0);
}

// Lee un numero de un input (por su id)
function leer(id) {
  return parseFloat(document.getElementById(id).value);
}

// Revisa que todos los numeros sean validos (mayores a 0)
function sonValidos(lista) {
  for (var i = 0; i < lista.length; i++) {
    if (isNaN(lista[i]) || lista[i] <= 0) {
      alert("Escribe todos los datos con numeros mayores a 0");
      return false;
    }
  }
  return true;
}

// Muestra el cuadro verde con los resultados
// lineas es una lista de pares: [["Texto", "valor"], ["Texto", "valor"]]
function mostrarResultado(idCaja, lineas) {
  var html = "<h3>Resultado</h3>";
  for (var i = 0; i < lineas.length; i++) {
    html += "<p>" + lineas[i][0] + " <b>" + lineas[i][1] + "</b></p>";
  }
  var caja = document.getElementById(idCaja);
  caja.innerHTML = html;
  caja.classList.remove("oculta");
}


// ---------- CONEXION CON EL BACKEND (JAVA) ----------

// Envia datos al servidor Java y ejecuta "alTerminar" si todo salio bien.
//  ruta:       a cual ruta llamar. Ej: "login"  ->  /api/login
//  datos:      objeto con lo que se envia. Ej: { usuario: "juan", clave: "123456" }
//  alTerminar: funcion que recibe las lineas de la respuesta (sin el "OK")
//
// El servidor SIEMPRE responde texto: la primera linea es "OK" o "ERROR"
// y las demas lineas son el mensaje o los datos.
function llamarApi(ruta, datos, alTerminar) {

  // Convertimos el objeto en texto: usuario=juan&clave=123456
  var partes = [];
  for (var clave in datos) {
    partes.push(encodeURIComponent(clave) + "=" + encodeURIComponent(datos[clave]));
  }

  fetch("/api/" + ruta, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: partes.join("&")
  })
  .then(function (respuesta) {
    return respuesta.text();
  })
  .then(function (texto) {
    // Separamos la respuesta por lineas y quitamos las vacias
    var lineas = texto.split("\n").filter(function (l) { return l != ""; });
    var estado = lineas[0];
    var contenido = lineas.slice(1);

    if (estado == "ERROR") {
      alert(contenido.join("\n"));
      return;
    }
    alTerminar(contenido);
  })
  .catch(function () {
    alert("No se pudo conectar con el servidor Java. Esta encendido? (java Servidor)");
  });
}

// Convierte las lineas "Texto;valor" que manda Java en el cuadro verde
function mostrarResultadoServidor(idCaja, contenido) {
  var pares = [];
  for (var i = 0; i < contenido.length; i++) {
    pares.push(contenido[i].split(";"));
  }
  mostrarResultado(idCaja, pares);
}


// ---------- 1. LOGIN ----------
// Ruta Java: /api/login  (READ)
function iniciarSesion() {
  var usuario = document.getElementById("loginUsuario").value.trim();
  var clave = document.getElementById("loginClave").value;

  llamarApi("login", { usuario: usuario, clave: clave }, function (contenido) {
    // Java responde "OK" y en la segunda linea el nombre completo
    var primerNombre = contenido[0].split(" ")[0];
    document.getElementById("saludo").innerText = "Hola, " + primerNombre;

    document.getElementById("loginUsuario").value = "";
    document.getElementById("loginClave").value = "";
    mostrarPantalla("menu");
  });
}


// ---------- 2. REGISTRO ----------
// Ruta Java: /api/registro  (CREATE)
function registrarUsuario() {
  var nombre = document.getElementById("regNombre").value.trim();
  var usuario = document.getElementById("regUsuario").value.trim();
  var clave = document.getElementById("regClave").value;
  var clave2 = document.getElementById("regClave2").value;

  // Las validaciones las hace Java (Servidor.registro)
  llamarApi("registro", { nombre: nombre, usuario: usuario, clave: clave, clave2: clave2 }, function (contenido) {
    alert(contenido[0]);
    document.getElementById("regNombre").value = "";
    document.getElementById("regUsuario").value = "";
    document.getElementById("regClave").value = "";
    document.getElementById("regClave2").value = "";
    mostrarPantalla("login");
  });
}


// ---------- 3. RECUPERAR USUARIO ----------
// Ruta Java: /api/recuperar  (UPDATE)
function recuperarCuenta() {
  var usuario = document.getElementById("recUsuario").value.trim();
  var nombre = document.getElementById("recNombre").value.trim();
  var claveNueva = document.getElementById("recClave").value;

  llamarApi("recuperar", { usuario: usuario, nombre: nombre, clave: claveNueva }, function (contenido) {
    alert(contenido[0]);
    document.getElementById("recUsuario").value = "";
    document.getElementById("recNombre").value = "";
    document.getElementById("recClave").value = "";
    mostrarPantalla("login");
  });
}


// ---------- 4. MENU: SALIR ----------
function cerrarSesion() {
  mostrarPantalla("login");
}


// ---------- 5. MURO DE LADRILLO ----------
// Cambia el boton activo de 20 cm / 30 cm
function elegirEspesor(cm) {
  espesorMuro = cm;
  document.getElementById("btn20").classList.remove("activo");
  document.getElementById("btn30").classList.remove("activo");
  document.getElementById("btn20").innerHTML = "20 cm";
  document.getElementById("btn30").innerHTML = "30 cm";

  document.getElementById("btn" + cm).classList.add("activo");
  document.getElementById("btn" + cm).innerHTML = cm + " cm &#10003;";
}

// Ruta Java: /api/muro
function calcularMuro() {
  var largo = leer("muroLargo");
  var alto = leer("muroAlto");
  if (!sonValidos([largo, alto])) return;

  llamarApi("muro", { largo: largo, alto: alto, espesor: espesorMuro }, function (contenido) {
    mostrarResultadoServidor("resMuro", contenido);
  });
}


// ---------- 6. VIGA DE HORMIGON ----------
// Ruta Java: /api/viga  (el ancho y el alto se envian en cm, Java los pasa a metros)
function calcularViga() {
  var largo = leer("vigaLargo");
  var ancho = leer("vigaAncho");
  var alto = leer("vigaAlto");
  var cantidad = leer("vigaCantidad");
  if (!sonValidos([largo, ancho, alto, cantidad])) return;

  llamarApi("viga", { largo: largo, ancho: ancho, alto: alto, cantidad: cantidad }, function (contenido) {
    mostrarResultadoServidor("resViga", contenido);
  });
}


// ---------- 7. COLUMNAS DE HORMIGON ----------
// Ruta Java: /api/columnas
function calcularColumnas() {
  var cantidad = leer("colCantidad");
  var alto = leer("colAlto");
  var lado1 = leer("colLado1");
  var lado2 = leer("colLado2");
  if (!sonValidos([cantidad, alto, lado1, lado2])) return;

  llamarApi("columnas", { cantidad: cantidad, alto: alto, lado1: lado1, lado2: lado2 }, function (contenido) {
    mostrarResultadoServidor("resColumnas", contenido);
  });
}


// ---------- 8. CONTRAPISO ----------
// Ruta Java: /api/contrapiso
function calcularContrapiso() {
  var largo = leer("conLargo");
  var ancho = leer("conAncho");
  var espesor = leer("conEspesor");
  if (!sonValidos([largo, ancho, espesor])) return;

  llamarApi("contrapiso", { largo: largo, ancho: ancho, espesor: espesor }, function (contenido) {
    mostrarResultadoServidor("resContrapiso", contenido);
  });
}


// ---------- 9. TECHO ----------
// Ruta Java: /api/techo
function calcularTecho() {
  var largo = leer("techoLargo");
  var ancho = leer("techoAncho");
  if (!sonValidos([largo, ancho])) return;

  llamarApi("techo", { largo: largo, ancho: ancho }, function (contenido) {
    mostrarResultadoServidor("resTecho", contenido);
  });
}


// ---------- 10. PISOS ----------
// Ruta Java: /api/pisos  ("lado" es el lado de la baldosa en cm)
function calcularPisos() {
  var largo = leer("pisoLargo");
  var ancho = leer("pisoAncho");
  var lado = leer("pisoBaldosa");
  if (!sonValidos([largo, ancho, lado])) return;

  llamarApi("pisos", { largo: largo, ancho: ancho, lado: lado }, function (contenido) {
    mostrarResultadoServidor("resPisos", contenido);
  });
}


// ---------- 11. PINTURA ----------
// Ruta Java: /api/pintura
function calcularPintura() {
  var largo = leer("pinLargo");
  var alto = leer("pinAlto");
  var manos = leer("pinManos");
  if (!sonValidos([largo, alto, manos])) return;

  llamarApi("pintura", { largo: largo, alto: alto, manos: manos }, function (contenido) {
    mostrarResultadoServidor("resPintura", contenido);
  });
}
