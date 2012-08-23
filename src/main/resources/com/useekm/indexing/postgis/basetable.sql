CREATE TABLE ${TABLE_NAME}
(
  objectdate timestamp without time zone,
  objectlanguage character varying(255) NOT NULL,
  objectstring text NOT NULL,
  objecttsvectorconfig text,
  objecttype text NOT NULL,
  objecturi boolean NOT NULL,
  predicate text NOT NULL,
  subject text NOT NULL,
  objecttsvector tsvector
) WITH (OIDS=FALSE);
SELECT AddGeometryColumn('${TABLE_NAME}', 'objectspatial', ${SRID}, 'GEOMETRY', ${DIMENSION});