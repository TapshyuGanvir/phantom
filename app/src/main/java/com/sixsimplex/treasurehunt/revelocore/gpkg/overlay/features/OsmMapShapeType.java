/**
 The MIT License

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.

 This code was sourced from the National Geospatial Intelligency Agency and was
 originally licensed under the MIT license. It has been modified to support
 osmdroid's APIs.

 You can find the original code base here:
 https://github.com/ngageoint/geopackage-android-map
 https://github.com/ngageoint/geopackage-android
 */

package com.sixsimplex.treasurehunt.revelocore.gpkg.overlay.features;

/**
 * Enumeration of Map shape types for handling geometry hierarchies
 * 
 * @author osbornb
 */
public enum OsmMapShapeType {

	LAT_LNG,

	MARKER_OPTIONS,

	POLYLINE_OPTIONS,

	POLYGON_OPTIONS,

	MULTI_LAT_LNG,

	MULTI_POLYLINE_OPTIONS,

	MULTI_POLYGON_OPTIONS,

	MARKER,

	POLYLINE,

	POLYGON,

	MULTI_MARKER,

	MULTI_POLYLINE,

	MULTI_POLYGON,

	POLYLINE_MARKERS,

	POLYGON_MARKERS,

	MULTI_POLYLINE_MARKERS,

	MULTI_POLYGON_MARKERS,

	COLLECTION;

}
