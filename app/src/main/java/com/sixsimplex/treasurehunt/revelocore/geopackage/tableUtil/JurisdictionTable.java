package com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil;

import android.content.Context;

import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.DbRelatedConstants;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageManagerAgent;
import com.sixsimplex.treasurehunt.revelocore.geopackage.models.Jurisdiction;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.sf.Geometry;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionTransform;


public class JurisdictionTable {

    private static final String JURISDICTIONS_TABLE_NAME = "jurisdictions";
    private static final String JURISDICTIONS_ROW_ID = "ROWID";
    private static final String JURISDICTIONS_NAME = "name";
    private static final String JURISDICTIONS_TYPE = "type";
    private static final String JURISDICTIONS_THE_GEOM = "THE_GEOM";

    private static FeatureDao jurisdictionDao;
    private static ProjectionTransform transform4326;

    private static FeatureDao getJurisdictionDao(Context context) {

        if (jurisdictionDao == null) {
            GeoPackage metaGeoPackage = GeoPackageManagerAgent.getMetaGeoPackage(context, DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context));
            if (metaGeoPackage != null) {
                jurisdictionDao = metaGeoPackage.getFeatureDao(JURISDICTIONS_TABLE_NAME);

                Projection projection = jurisdictionDao.getProjection();
                transform4326 = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            }
        }
        return jurisdictionDao;
    }

    public static List<Jurisdiction> getAssignJurisdiction(Context context) {

        List<Jurisdiction> assignJurisdictionGeometry = new ArrayList<>();

        try {

            FeatureDao jurisdictionDao = getJurisdictionDao(context);
            if (jurisdictionDao != null) {

                try (FeatureCursor featureCursor = jurisdictionDao.queryForAll()) {

                    while (featureCursor.moveToNext()) {

                        try {

                            FeatureRow featureRow = featureCursor.getRow();

                            String jurisdictionName = "";
                            Object name = featureRow.getValue(JURISDICTIONS_NAME);
                            if (name != null) {
                                jurisdictionName = String.valueOf(name);
                            }

                            String jurisdictionType = "";
                            Object type = featureRow.getValue(JURISDICTIONS_TYPE);
                            if (type != null) {
                                jurisdictionType = String.valueOf(type);
                            }

                            GeoPackageGeometryData geometryData = featureRow.getGeometry();
                            if (geometryData != null) {

                                GeoPackageGeometryData geometryData4326 = geometryData.transform(transform4326);
                                geometryData4326.setSrsId(4326);

                                Geometry geometry = geometryData4326.getGeometry();

                                if (geometry != null) {

                                    Jurisdiction jurisdiction = new Jurisdiction();
                                    jurisdiction.setGeoPackageGeometryData(geometryData4326);

                                    jurisdiction.setName(jurisdictionName);
                                    jurisdiction.setType(jurisdictionType);
                                    jurisdiction.setGeometry(geometry);

                                    assignJurisdictionGeometry.add(jurisdiction);
                                }
                            }
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

        return assignJurisdictionGeometry;
    }

    public static void clearJurisdictions() {
        if (jurisdictionDao != null) {
            jurisdictionDao = null;
        }
    }

    public static boolean storeSelectedJurisdictions(Context context,Geometry geometry, String name, String type) {

        boolean isSelectedGeomAdded = false;
        try {
            if (jurisdictionDao != null) {

                FeatureRow row = jurisdictionDao.newRow();

                row.setValue(JURISDICTIONS_NAME, name);
                row.setValue(JURISDICTIONS_TYPE, type);

                GeoPackageGeometryData geomData = new GeoPackageGeometryData(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                geomData.setGeometry(geometry);
                row.setGeometry(geomData);

                long result = jurisdictionDao.insert(row);

                if (result == -1) {
                    ReveloLogger.debug(JURISDICTIONS_TABLE_NAME, "storeSelectedJurisdictions", "Selected jurisdiction not saved.");
                } else {
                    isSelectedGeomAdded = true;
                    ReveloLogger.debug(JURISDICTIONS_TABLE_NAME, "storeSelectedJurisdictions", "Selected jurisdiction saved.");
                }

                GeoPackageManagerAgent.exportDataGeopackage(context);  // export database here.
                GeoPackageManagerAgent.exportMetaDataGeopackage(context);  // export metadat here.
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(JURISDICTIONS_TABLE_NAME, "storeSelectedJurisdictions", "Selected jurisdiction not saved. Error: " + e.getMessage());
        }
        return isSelectedGeomAdded;
    }
}