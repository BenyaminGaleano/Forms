# Forms
En la carpeta **input** coloque el archivo csv descargado de Google Forms.
En la carpeta **output** coloque los archivos csv descargados del ges, pueden ser varios ya que el form pudo haber recolectado información de más de una sección.

### Descarga
toca [aquí](https://benyamingaleano.github.io/files/forms.zip) para descargar la versión actual, el código de este repositorio no es necesario para calificar.

### Notas Importantes "Leer"
Ve el [video](https://drive.google.com/file/d/1fRRwLKIDQzzHfz8ApVW54PiKTGk653yD/view?usp=sharing) si no quieres leer.

Los archivos yaml que se encuentran en el directorio principal son para las preferencias todos los campos empiezan desde **"1"** no desde **"0"**, para un buen funcionamiento verifique que la hoja que está calificando tenga la configuración correcta según los yaml:

- En general como convenio si termina con Col este es una configuracion de columna entonces es devuelta en la lista del método getAllCols.
- **idCol** el número de columna de la identificación del alumno.
- **nameCol** el número de columna del nombre del alumano.
- **startline** número de linea donde inicia los datos no se cuentan las etiquetas de columna, sólo los datos puros.
- **gradeCol** el número de columna donde va la nota.
- **classCol** el número de columna donde va la sección.
- **grade** nota máxima para los alumnos, como este trabaja con google forms y el formato es xxx/yyy donde xxx es la nota que sacó el alumno y yyy es el máximo, los divide y así obtiene el porcentaje de ahí puede reponderar la nota colocando una nota en grade.
- **commentCol** el número de columna para el comentario.
- **comment** un comentario simple.
- **classes** contiene una lista de secciones que se toman en cuenta, el resto son ignoradas, coloque **all** si quiere calificar todas, para más información lea el comentario sobre dicho campo en el archivo input.yaml.

### Ejecutar en terminal
- en **GNU/Linux** o **MacOS** bin/forms
- en **Windows** bin/forms.bat

### Versionado
se usará el versionado semántico el cual puede consultar en este [link](https://semver.org/lang/es/)
