package com.example.homepage.model

import com.google.firebase.database.IgnoreExtraProperties

//@IgnoreExtraProperties
class Case {
    var suburb: String
    var siteTitle: String
    var siteStreet: String
    var siteState: String
    var sitePostcode: String
    var exposureDate: String
    var exposureTime: String
    var notes: String
    var addedDate: String
    var addedTime: String
    var adviceTitle: String
    var adviceInstruction: String
    var exposureTimeStart: String
    var exposureTimeEnd: String
    var dhid: String

    constructor(
        suburb: String,
        siteTitle: String,
        siteStreet: String,
        siteState: String,
        sitePostcode: String,
        exposureDate: String,
        exposureTime: String,
        notes: String,
        addedDate: String,
        addedTime: String,
        adviceTitle: String,
        adviceInstruction: String,
        exposureTimeStart: String,
        exposureTimeEnd: String,
        dhid: String
    ) {
        this.suburb = suburb
        this.siteTitle = siteTitle
        this.siteStreet = siteStreet
        this.siteState = siteState
        this.sitePostcode = sitePostcode
        this.exposureDate = exposureDate
        this.exposureTime = exposureTime
        this.notes = notes
        this.addedDate = addedDate
        this.addedTime = addedTime
        this.adviceTitle = adviceTitle
        this.adviceInstruction = adviceInstruction
        this.exposureTimeStart = exposureTimeStart
        this.exposureTimeEnd = exposureTimeEnd
        this.dhid = dhid
    }
}