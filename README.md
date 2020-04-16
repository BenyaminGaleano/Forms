# Forms
En la carpeta **input** coloque el archivo csv descargado de Google Forms.
En la carpeta **output** coloque los archivos csv descargados del ges, pueden ser varios ya que el form pudo haber recolectado información de más de una sección.

### Notas
Los archivos config que se encuentran en el archivo principal son para las preferencias:

- En general como convenio si termina con Col este es una configuracion de columna entonces es devuelta en la lista del método getAllCols.
- **idCol** el número de columna de la identificación del alumno.
- **nameCol** el número de columna del nombre del alumano.
- **startline** número de linea donde inicia los datos no se cuentan las etiquetas de columna, sólo los datos puros.
- **grade** nota máxima para los alumnos, como este trabaja con google forms y el formato es xxx/yyy donde xxx es la nota que sacó el alumno y yyy es el máximo, los divide y así obtiene el porcentaje de ahí puede reponderar la nota colocando una nota en grade.