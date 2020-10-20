# Differences with...

### **Differences between Airbyte and Singer**

We wrote an article about this topic: “[Airbyte vs. Singer: Why Airbyte is not built on top of Singer](https://airbyte.io/articles/data-engineering-thoughts/airbyte-vs-singer-why-airbyte-is-not-built-on-top-of-singer/).” As a summary, here are the differences. 

[**Singer**](https://singer.io/)**:**

* Supports 96 connectors after 4 years.
* Increasingly outdated connectors: Talend \(acquirer of StitchData\) seems to have stopped investing in maintaining Singer’s community and connectors. As most connectors see schema changes several times a year, more and more Singer’s taps and targets are not actively maintained and are becoming outdated. 
* Absence of standardization: each connector is its own open-source project. So you never know the quality of a tap or target until you have actually used it. There is no guarantee whatsoever about what you’ll get.
* Singer’s connectors are standalone binaries: you still need to build everything around to make them work. 
* No full commitment to open sourcing all connectors, as some connectors are only offered by StitchData under a paid plan.  ****

[**Airbyte**](http://airbyte.io/)**:**

* Our ambition is to support 50+ connectors by the end of 2020, just 5 months after its inception. 
* Airbyte’s connectors are usable out of the box through a UI and API, with monitoring, scheduling and orchestration. Airbyte was built on the premise that a user, whatever their background, should be able to move data in 2 minutes. Data engineers might want to use raw data and their own transformation processes, or to use Airbyte’s API to include data integration in their workflows. On the other hand, analysts and data scientists might want to use normalized consolidated data in their database or data warehouses. Airbyte supports all these use cases.  
* One platform, one project with standards: This will help consolidate the developments behind one single project, some standardization and specific data protocol that can benefit all teams and specific cases. 
* Connectors can be built in the language of your choice, as Airbyte runs them as Docker containers.
* Decoupling of the whole platform to let teams use whatever part of Airbyte they want based on their needs and their existing stack \(orchestration with Airflow, Kubernetes, or Airbyte, transformation with [DBT](http://getdbt.com) or again Airbyte, etc.\). Teams can use Airbyte’s orchestrator or not, their normalization or not; everything becomes possible. 
* A full commitment to the open-source MIT project with the promise not to hide some connectors behind paid walls.

Note that Airbyte’s data protocol is compatible with Singer’s. So it is easy to migrate a Singer tap onto Airbyte. 

#### **Differences between Airbyte and Pipelinewise**

[**PipelineWise**](https://github.com/transferwise/pipelinewise)**:** 

**PipelineWise is an open-source project by Transferwise that was built with the primary goal of serving their own needs. There is no business model attached to the project, and no apparent interest in growing the community.** 

* **Supports 21 connectors, and only adds new ones based on the needs of the mother company, Transferwise.** 
* **No business model attached to the project, and no apparent interest from the company in growing the community.** 
* **As close to the original format as possible: PipelineWise aims to reproduce the data from the source to an Analytics-Data-Store in as close to the original format as possible. Some minor load time transformations are supported, but complex mapping and joins have to be done in the Analytics-Data-Store to extract meaning.**
* **Managed Schema Changes: When source data changes, PipelineWise detects the change and alters the schema in your Analytics-Data-Store automatically.**
* **YAML based configuration: Data pipelines are defined as YAML files, ensuring that the entire configuration is kept under version control.**
* **Lightweight: No daemons or database setup are required.**

[**Airbyte**](http://airbyte.io/)**:**

**In contrast, Airbyte is  a company fully committed to the open-source MIT project and has a** [**business model in mind**](https://docs.airbyte.io/company-handbook/company-handbook/business-model) **around this project.**

* **Our ambition is to support 50+ connectors by the end of 2020, just 5 months after its inception.** 
* **Airbyte’s connectors are usable out of the box through a UI and API, with monitoring, scheduling and orchestration. Airbyte was built on the premise that a user, whatever their background, should be able to move data in 2 minutes. Data engineers might want to use raw data and their own transformation processes, or to use Airbyte’s API to include data integration in their workflows. On the other hand, analysts and data scientists might want to use normalized consolidated data in their database or data warehouses. Airbyte supports all these use cases.**  
* **One platform, one project with standards: This will help consolidate the developments behind one single project, some standardization and specific data protocol that can benefit all teams and specific cases.** 
* **Connectors can be built in the language of your choice, as Airbyte runs them as Docker containers.**
* **Decoupling of the whole platform to let teams use whatever part of Airbyte they want based on their needs and their existing stack \(orchestration with Airflow, Kubernetes, or Airbyte, transformation with** [**DBT**](http://getdbt.com) **or again Airbyte, etc.\). Teams can use Airbyte’s orchestrator or not, their normalization or not; everything becomes possible.** 

**The data protocols for both projects are compatible with Singer’s. So it is easy to migrate a Singer tap or target onto Airbyte or PipelineWise.**

#### **Differences between Airbyte and Meltano**

[**Meltano**](http://meltano.com)**:**

**Meltano is a Gitlab side project. Since 2019, they have been iterating on several approaches. The latest positioning is an orchestrator dedicated to data integration that was built by Gitlab on top of Singer’s taps and targets. They now have only one maintainer for this project.** 

* **Only 19 connectors built on top of Singer, after more than a year. This means that Meltano has the same limitations as Singer in regards to its data protocol.** 
* **CLI-first approach: Meltano was primarily built with a command line interface in mind. In that sense, they seem to target engineers with a preference for that interface. Unfortunately, it’s not thought to be part of some workflows.** 
* **A new UI: Meltano has recently built a new UI to try to appeal to a larger audience.** 
* **Integration with DBT for transformation: Meltano offers some deep integration with** [**DBT**](http://getdbt.com)**, and therefore lets data engineering teams handle transformation any way they want.** 
* **Integration with Airflow for orchestration: You can either use Meltano alone for orchestration or with Airflow; Meltano works both ways.** 

[**Airbyte**](http://airbyte.io/)**:**

**In contrast, Airbyte is a company fully committed to the open-source MIT project and has a** [**business model in mind**](https://docs.airbyte.io/company-handbook/company-handbook/business-model) **around this project. Our** [**team**](https://docs.airbyte.io/company-handbook/company-handbook/team) **are data integration experts that have built more than 1,000 integrations collectively at large scale.** 

* **Our ambition is to support 50+ connectors by the end of 2020, just 5 months after our inception.** 
* **Airbyte’s connectors are usable out of the box through a UI and API, with monitoring, scheduling and orchestration. Airbyte was built on the premise that a user, whatever their background, should be able to move data in 2 minutes. Data engineers might want to use raw data and their own transformation processes, or to use Airbyte’s API to include data integration in their workflows. On the other hand, analysts and data scientists might want to use normalized consolidated data in their database or data warehouses. Airbyte supports all these use cases.**  
* **One platform, one project with standards: This will help consolidate the developments behind one single project, some standardization and specific data protocol that can benefit all teams and specific cases.** 
* **Not limited by Singer’s data protocol: In contrast to Meltano, Airbyte was not built on top of Singer, but its data protocol is compatible with Singer’s. This means Airbyte can go beyond Singer, but Meltano will remain limited.** 
* **Connectors can be built in the language of your choice, as Airbyte runs them as Docker containers.**
* **Decoupling of the whole platform to let teams use whatever part of Airbyte they want based on their needs and their existing stack \(orchestration with Airflow, Kubernetes, or Airbyte, transformation with DBT or again Airbyte, etc.\). Teams can use Airbyte’s orchestrator or not, their normalization or not; everything becomes possible.** 

#### **Differences between Airbyte and Fivetran**

**We wrote an article, “**[**Open-source vs. Commercial Software: How to Solve the Data Integration Problem**](https://airbyte.io/articles/data-engineering-thoughts/open-source-vs-commercial-software-how-to-better-solve-data-integration/)**,” in which we describe the pros and cons of Fivetran’s commercial approach and Airbyte’s open-source approach. Don’t hesitate to check it out for more detailed arguments. As a summary, here are the differences:** 

![](https://lh6.googleusercontent.com/XR86I4_nMdxQrEgyyHBVIxexupMpwVjW2WayONmdG17UydfB_qSKnGRkp1y5MHAwa16FmF7LDIRGBZEnQd2BtrioqVi3jwvi-1G_dNEL5_vprEj_P_QKazvQfx371HR7uApP7Z5l)

**Fivetran:**

* **Limited high-quality connectors: after 8 years in business, Fivetran only supports 150 connectors. The more connectors, the more difficult it is for Fivetran to keep the same level of maintenance across all connectors.**
* **Pricing indexed on usage: Fivetran’s pricing is indexed on the connectors used and the volume of data transferred. Teams always need to keep that in mind and are not free to move data without thinking about cost.** 
* **Security and privacy compliance: all companies are subject to privacy compliance laws, such as GDPR, CCPA, HIPAA, etc. As a matter of fact, above a certain stage \(about 100 employees\) in a company, all external products need to go through a security compliance process that can take several months.** 
* **No moving data between internal databases: Fivetran sits in the cloud, so if you have to replicate data from an internal database to another, it makes no sense to have the data move through them \(Fivetran\) for privacy and cost reasons.** 

**Airbyte:**

* **Free, as open source, so no more pricing based on usage: learn more about our** [**future business model**](https://docs.airbyte.io/company-handbook/company-handbook/business-model) **\(connectors will always remain open source\).** 
* **Supporting 50+ connectors by the end of 2020 \(so in only 5 months of existence\).**
* **Building new connectors made trivial, in the language of your choice: Airbyte makes it a lot easier to create your own connector, vs. building them yourself in-house \(with Airflow or other tools\). Scheduling, orchestration, and monitoring comes out of the box with Airbyte.**
* **Addressing the long tail of connectors: with the help of the community, Airbyte ambitions to support thousands of connectors.** 
* **Adapt existing connectors to your needs: you can adapt any existing connector to address your own unique edge case.**
* **Using data integration in a workflow: Airbyte’s API lets engineering teams add data integration jobs into their workflow seamlessly.** 
* **Debugging autonomy: if you experience any connector issue, you won’t need to wait for Fivetran’s customer support team to get back to you, if you can fix the issue fast yourself.** 
* **No more security and privacy compliance, as self-hosted and open-sourced \(MIT\). Any team can directly address their integration needs.**
* **Your data stays in your cloud. Have full control over your data, and the costs of your data transfers.**

#### **Differences between Airbyte and StitchData**

**We wrote an article, “**[**Open-source vs. Commercial Software: How to Solve the Data Integration Problem**](https://airbyte.io/articles/data-engineering-thoughts/open-source-vs-commercial-software-how-to-better-solve-data-integration/)**,” in which we describe the pros and cons of StitchData’s commercial approach and Airbyte’s open-source approach. Don’t hesitate to check it out for more detailed arguments. As a summary, here are the differences:**  


**StitchData:**

* **Limited deprecating connectors: Stitch only supports 150 connectors. Talend has stopped investing in StitchData and its connectors. And on Singer, each connector is its own open-source project. So you never know the quality of a tap or target until you have actually used it. There is no guarantee whatsoever about what you’ll get.**
* **Pricing indexed on usage: StitchData’s pricing is indexed on the connectors used and the volume of data transferred. Teams always need to keep that in mind and are not free to move data without thinking about cost.** 
* **Security and privacy compliance: all companies are subject to privacy compliance laws, such as GDPR, CCPA, HIPAA, etc. As a matter of fact, above a certain stage \(about 100 employees\) in a company, all external products need to go through a security compliance process that can take several months.** 
* **No moving data between internal databases: StitchData sits in the cloud, so if you have to replicate data from an internal database to another, it makes no sense to have the data move through their cloud for privacy and cost reasons.** 
* **StitchData’s Singer connectors are standalone binaries: you still need to build everything around to make them work. And it’s hard to update some pre-built connectors, as they are of poor quality.** 

**Airbyte:**

* **Free, as open source, so no more pricing based on usage: learn more about our** [**future business model**](https://docs.airbyte.io/company-handbook/company-handbook/business-model) **\(connectors will always remain open-source\).** 
* **Supporting 50+ connectors by the end of 2020 \(so in only 5 months of existence\).**
* **Building new connectors made trivial, in the language of your choice: Airbyte makes it a lot easier to create your own connector, vs. building them yourself in-house \(with Airflow or other tools\). Scheduling, orchestration, and monitoring comes out of the box with Airbyte.**
* **Maintenance-free connectors you can use in minutes. Just authenticate your sources and warehouse, and get connectors that adapt to schema and API changes for you.**
* **Addressing the long tail of connectors: with the help of the community, Airbyte ambitions to support thousands of connectors.** 
* **Adapt existing connectors to your needs: you can adapt any existing connector to address your own unique edge case.**
* **Using data integration in a workflow: Airbyte’s API lets engineering teams add data integration jobs into their workflow seamlessly.** 
* **Debugging autonomy: if you experience any connector issue, you won’t need to wait for Fivetran’s customer support team to get back to you, if you can fix the issue fast yourself.** 
* **Your data stays in your cloud. Have full control over your data, and the costs of your data transfers.**
* **No more security and privacy compliance, as self-hosted and open-sourced \(MIT\). Any team can directly address their integration needs.**

**Decoupling of Extract-Load from Transformation: a normalization stands for an opinionated view of how one should use the data. Airbyte enables engineers, who want to transform the data themselves with their processes, to do that. It also enables engineers / analysts / data scientists / teams, to use the normalized data right out of the box, if this is in line with what they need.**

