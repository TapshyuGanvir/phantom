package com.sixsimplex.treasurehunt.Phantom1.model;

public class TraversalFeature implements Comparable<TraversalFeature> {

    public enum VisitingStatus {VISITED, SKIPPED, ONGOING, NOTVISITED}
    int sequenceNo;
    String sequenceLabel;
    VisitingStatus visitingStatus;
    String featureId;

    public TraversalFeature(int sequenceNo, String sequenceLabel,
                            VisitingStatus visitingStatus,
                            String featureId) {
        this.sequenceNo = sequenceNo;
        this.sequenceLabel = sequenceLabel;
        this.visitingStatus = visitingStatus;
        this.featureId = featureId;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(int sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    public String getSequenceLabel() {
        return sequenceLabel;
    }

    public void setSequenceLabel(String sequenceLabel) {
        this.sequenceLabel = sequenceLabel;
    }

    public VisitingStatus getVisitingStatus() {
        return visitingStatus;
    }

    public void setVisitingStatus(VisitingStatus visitingStatus) {
        this.visitingStatus = visitingStatus;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    @Override
    public int compareTo(TraversalFeature traversalFeature) {
        if(sequenceNo< traversalFeature.getSequenceNo())
            return -1;
        else if(sequenceNo> traversalFeature.getSequenceNo()){
            return 1;
        }
        return 0;
    }
}
