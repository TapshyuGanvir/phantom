package com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil;

import android.content.Context;

import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.DbRelatedConstants;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageManagerAgent;
import com.sixsimplex.treasurehunt.revelocore.geopackage.models.RelationShipModel;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.attributes.AttributesCursor;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesRow;

public class RelationShipTable {

    /*------------------------- relationships table -------------------------*/

    private static final String RELATIONSHIPS_TABLE_NAME = "relationships";
    private static final String RELATIONSHIPS_NAME = "name";
    private static final String RELATIONSHIPS_FROM_COL = "fromcol";
    private static final String RELATIONSHIPS_TO_COL = "tocol";
    private static final String RELATIONSHIPS_FROM_ID_COL = "fromidcol";
    private static final String RELATIONSHIPS_TO_ID_COL = "toidcol";

    /*-----------------------------------------------------------------------*/

    private static AttributesDao dao;

    private static AttributesDao getDao(Context context) {
        try {
            if (dao == null) {
                GeoPackage metaGeoPackage = GeoPackageManagerAgent.getMetaGeoPackage(context, DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context));
                if (metaGeoPackage != null) {
                    dao = metaGeoPackage.getAttributesDao(RELATIONSHIPS_TABLE_NAME);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dao;
    }

    public static List<RelationShipModel> getAllRelations(Context context) {

        List<RelationShipModel> relationList = new ArrayList<>();

        try {

            AttributesDao relationShipDao = getDao(context);

            if (relationShipDao != null) {

                try (AttributesCursor attributesCursor = relationShipDao.queryForAll()) {

                    while (attributesCursor.moveToNext()) {

                        try {

                            AttributesRow row = attributesCursor.getRow();

                            String name = "";
                            Object na = row.getValue(RELATIONSHIPS_NAME);
                            if (na != null) {
                                name = String.valueOf(na);
                            }

                            String fromCol = "";
                            Object frCol = row.getValue(RELATIONSHIPS_FROM_COL);
                            if (frCol != null) {
                                fromCol = String.valueOf(frCol);
                            }

                            String toCol = "";
                            Object tCol = row.getValue(RELATIONSHIPS_TO_COL);
                            if (tCol != null) {
                                toCol = String.valueOf(tCol);
                            }

                            String fromIdCol = "";
                            Object frIdCol = row.getValue(RELATIONSHIPS_FROM_ID_COL);
                            if (frIdCol != null) {
                                fromIdCol = String.valueOf(frIdCol);
                            }

                            String toIdCol = "";
                            Object tIdCol = row.getValue(RELATIONSHIPS_TO_ID_COL);
                            if (tIdCol != null) {
                                toIdCol = String.valueOf(tIdCol);
                            }

                            RelationShipModel relationShip = new RelationShipModel();
                            relationShip.setName(name);
                            relationShip.setFromCol(fromCol);
                            relationShip.setToCol(toCol);
                            relationShip.setFromIdCol(fromIdCol);
                            relationShip.setToIdCol(toIdCol);

                            relationList.add(relationShip);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return relationList;
    }

    public static void clearAttributeDao() {
        if (dao != null) {
            dao = null;
        }
    }
}
