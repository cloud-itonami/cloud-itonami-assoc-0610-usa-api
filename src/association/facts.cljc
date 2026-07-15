(ns association.facts
  "Industry rule/policy-statement catalog for the American Petroleum
  Institute (API, Wikidata Q466043) -- a 29th industry-association-
  level source (see cloud-itonami-assoc-6419-jpn-zenginkyo, -6512-jpn-sonpo,
  -6612-jpn-jsda, -6419-deu-bankenverband, -6612-usa-finra, -6512-usa-naic,
  -6920-jpn-jicpa, -6920-usa-aicpa, -6419-fra-fbf, -6511-jpn-seiho,
  -6910-jpn-nichibenren, -6810-jpn-recaj, -6411-jpn-boj, -6120-usa-ctia,
  -5110-usa-a4a, -3510-usa-eei, -2910-deu-vda, -5510-usa-ahla,
  -2100-usa-phrma, -4719-usa-nrf, -4100-usa-agc, -6020-usa-nab,
  -3600-usa-awwa, -4923-usa-ata, -5610-usa-nra, -2011-usa-acc,
  -8621-usa-ama, -6201-usa-gtia for the first twenty-eight) per
  ADR-2607141700 (cloud-itonami-compliance-fact-federation). The FIRST
  entry aligned to ISIC 0610 (extraction of crude petroleum) -- a new
  industry code for this family. A rule not in this table has NO
  spec-basis, full stop; extend `catalog`, do not invent an
  id/url/date.

  NOTE: 'API' here is the American Petroleum Institute, NOT a software
  Application Programming Interface -- disambiguated explicitly in
  this docstring, organization.edn, and README given the shared
  initialism.

  Both entries were directly WebFetch-verified against API's own
  pages: 'About API' states the exact founding date (March 20, 1919)
  in its own text, and API's 'Standards' history microsite states the
  exact publication date (October 20, 1924) of API's first-ever
  standard, 'Specifications for Steel and Iron Pipe for Oil Country
  Tubular Goods'.")

(def catalog
  "assoc-slug -> vector of self-regulatory rule entries."
  {"api"
   [{:association-rule/id "api.about-api"
     :association-rule/title "About API"
     :association-rule/association "api"
     :association-rule/isic "0610"
     :association-rule/country "USA"
     :association-rule/kind :governance-program
     :association-rule/url "https://www.api.org/about"
     :association-rule/url-provenance :official-association-site
     :association-rule/established-date "1919-03-20"
     :association-rule/retrieved-at "2026-07-16"
     :association-rule/topic #{:governance}}
    {:association-rule/id "api.first-standard-oil-country-tubular-goods"
     :association-rule/title "Specifications for Steel and Iron Pipe for Oil Country Tubular Goods (API's first standard)"
     :association-rule/association "api"
     :association-rule/isic "0610"
     :association-rule/country "USA"
     :association-rule/kind :technical-standard
     :association-rule/url "https://100yearsofstandards.api.org/"
     :association-rule/url-provenance :official-association-site
     :association-rule/established-date "1924-10-20"
     :association-rule/retrieved-at "2026-07-16"
     :association-rule/topic #{:technical-standard :safety}}]})

(defn spec-basis [assoc-slug] (get catalog assoc-slug))

(defn coverage
  ([] (coverage (keys catalog)))
  ([slugs]
   (let [have (filter catalog slugs)
         missing (remove catalog slugs)]
     {:requested (count slugs)
      :covered (count have)
      :covered-associations (vec (sort have))
      :missing-associations (vec (sort missing))
      :note (str "cloud-itonami-assoc-0610-usa-api Wave 0 (ADR-2607141700): "
                 (count (get catalog "api")) " api entries seeded with an "
                 "official api.org citation. Extend "
                 "`association.facts/catalog`, never fabricate a rule id/url.")})))

(defn by-topic [assoc-slug topic]
  (filterv #(contains? (:association-rule/topic %) topic) (spec-basis assoc-slug)))
