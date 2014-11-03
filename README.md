# kixi.health-data

A Clojure library designed to munge and explore open data on health
from HSCIC and others.

## Build Status

[![Build Status](https://travis-ci.org/MastodonC/kixi.health-data.svg)](https://travis-ci.org/MastodonC/kixi.health-data)


## Usage

At the moment the best way to use this is to fire up your repl and
call some of the functions.

### Data Sources


#### Data License

All of this data is available under the Open Government
License. Details of the license are available here:

http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/

#### GP Practice Prescribing Data

There are a number of data sources we use to analyse NHS Prescription
data. Currently we are only analysing data for NHS England.
Prescription data at the presentation level for GP Surgeries exists
for the NHS in England, Wales and Northern Ireland. NHS Scotland does
not currently produce prescription data at the presentation level for
GP surgeries, but they have plans to do it during 2014-2015.

##### England

The GP practice prescribing data - Presentation level is  available at
data.gov.uk here:

http://data.gov.uk/dataset/prescribing-by-gp-practice-presentation-level

##### Wales

The GP Practice prescribing data for Wales is available here:

http://www.wales.nhs.uk/sites3/page.cfm?orgid=428&pid=65866

##### Northern Ireland

The GP Practice prescribing data for Northern Ireland is available
here:

http://www.hscbusiness.hscni.net/services/2471.htm

#### British National Formulary

The British National Formulary is available here:

http://www.evidence.nhs.uk/formulary/bnf/current

This gives you a way of understanding the format of BNF numbers and
allows you to understand how particular medicines are used to treat
different categories of illness. It does not give you a one to one
mapping of illness to drug though and should be used with care.

#### GP Practice Organisational Data

The names and mapping of GP Practices in England to Clinical
Commissioning Groups is done by referencing the Details of GPs, GP
Practices, Nurses and Pharmacies from Organisation Data Service
available from data.gov.uk here:

http://data.gov.uk/dataset/england-nhs-connecting-for-health-organisation-data-service-data-files-of-general-medical-practices

Specifically this file:

http://systems.hscic.gov.uk/data/ods/datadownloads/data-files/epraccur.zip

#### CCG Names and other data

CCG Names are available from NHS England here:

http://www.england.nhs.uk/resources/ccg-directory/

Specifically this file:

http://www.england.nhs.uk/wp-content/uploads/2012/11/final-ccg-rca.xls

#### GP Practice and CCG Population Data

The number of patients at each GP Practice was found at data.gov.uk
here:

http://data.gov.uk/dataset/numbers_of_patients_registered_at_a_gp_practice

Specifically this file from HSCIC:

http://www.hscic.gov.uk/catalogue/PUB14505/gp-reg-patients-07-2014.csv

The number of patients registered at a CCG for October 2014 is
available from HSCIC here:

http://www.hscic.gov.uk/article/2021/Website-Search?productid=16172

Specifically this file:

http://www.hscic.gov.uk/catalogue/PUB15644/ccg-reg-patients-10-2014.csv

### Analysis

The code used to analyse this data is available on the
[MastodonC/kixi.health-data](http://github.com/MastodonC/kixi.health-data)
at github.

Most of the code is there to pull out specific parts of the monthly GP Surgery
prescriptions pdpi files using subsets of the BNF codes to find
particular classes of drugs. The rest of the code is used to group by
Surgery, CCG or chemical and then used to pull out the top and bottom
in the groups.

Finally there is code for enriching the records with human friendly
names by matching the identifiers to the reference data.

## License

Copyright © 2014 Mastodon C Ltd

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
