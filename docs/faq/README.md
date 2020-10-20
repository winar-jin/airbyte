# FAQ

### **How long does it take to set up Airbyte?**

It depends on your source and destination. Check our setup guides to see the tasks for your source and destination. Each source and destination also has a list of prerequisites for setup. To make setup faster, get your prerequisites ready before you start to set up your connector. During the setup process, you may need to contact others \(like a database administrator or AWS account owner\) for help, which might slow you down.

### **Where can I see my data in Airbyte?**

You can’t see your data in Airbyte, because we don’t store it. The sync loads your data into your destination \(data warehouse, data lake, etc.\). While you can’t see your data directly in Airbyte, you can check your schema and sync status on the source detail page in Airbyte.

### **Why don’t I see any data in my destination yet?**

It can take a while for Airbyte to load data into your destination. Some sources have restrictive API limits which constrain how much data we can sync in a given time. Large amounts of data in your source can also make the initial sync take longer. You can check your sync status on your source detail page.

### **What happens if a sync fails?**

You do not lose data when a sync fails, but no data is added or updated in your destination. Airbyte will show a notification on its UI in the Sources section and in the corresponding source detail page, so that you can begin troubleshooting. 

In the future, we might send you an email notification \(optional, of course\) with an additional option to create a GitHub issue with the logs. We’re still thinking about it, but the purpose would be to help the community and the Airbyte team fix the issue as soon as possible, if it’s an issue with the connector. 

In the meantime, here is what you can do now:

* **File a GitHub issue:** go [here](https://github.com/airbytehq/airbyte/issues/new?assignees=&labels=type%2Fbug&template=bug-report.md&title=) and file an issue with the detailed logs copied in the issue’s description. The team will be notified about your issue and will update it for any progress or comment on it. 
* **Fix the issue yourself:** with open source, you don’t need to wait for anybody to fix your issue if it is important to you. In that case, just fork the [GitHub project](http://github.com/airbytehq/airbyte) and fix the connector you need fixed. If you’re okay with contributing your fix to the community, please then create a pull request. Don’t hesitate to ping the team on [Slack](http://slack.airbyte.io), so we can check your PR as soon as possible. But you do NOT need to wait for the PR to be approved to benefit from your own fix. Put your connector in a new folder, and add your connector directly through our UI by clicking on + New connector in the Admin section. This way, you will be able to use your connector as a separate one from the connector available to the community. 

Once all this is done, Airbyte resumes your sync from where it left off.

We truly appreciate any contribution you make to help the community. Airbyte will become the open-source standard only if everybody participates. 

### **What happens to data in the pipeline if the destination gets disconnected? Could I lose data, or wind up with duplicate data when the pipeline is reconnected?**

Airbyte is architected to prevent data loss or duplication. We will display a failure for the sync, and re-attempt it at the next syncing, according to the frequency you set.

### **How does Airbyte handle replication when a data source changes its schema?**

How Airbyte handles data structure changes in a data source varies depending on the integration, as well as the replication method used for a given table within that integration.

However, Airbyte decouples the Extract and Load of a source from the normalization of the data at the destination. So the data will still land in your destination, but if you used any normalization, this part might be broken because of the schema change. 

At that point, you should see a failure displayed in Airbyte’s UI at the source level, along with all the detailed logs. You then have two options: 

* **File a GitHub issue:** go [here](https://github.com/airbytehq/airbyte/issues/new?assignees=&labels=type%2Fbug&template=bug-report.md&title=) and file an issue with the detailed logs copied in the issue’s description. The team will be notified about your issue and will update it for any progress or comment on it. 
* **Fix the issue yourself:** with open source, you don’t need to wait for anybody to fix your issue if it is important to you. In that case, just fork the [GitHub project](http://github.com/airbytehq/airbyte) and fix the connector you need fixed. If you’re okay with contributing your fix to the community, please then create a pull request. Don’t hesitate to ping the team on [Slack](http://slack.airbyte.io), so we can check your PR as soon as possible. But you do NOT need to wait for the PR to be approved to benefit from your own fix. Put your connector in a new folder, and add your connector directly through our UI by clicking on + New connector in the Admin section. This way, you will be able to use your connector as a separate one from the connector available to the community. 

### **How secure is Airbyte?**

Airbyte is an open-source self-hosted solution, so let’s say it is as safe as your data infrastructure. 

### **How does Airbyte charge?**

Well, we don’t, as the connectors are all under the MIT license. If you are curious about the business model we have in mind, please check our [company handbook](https://docs.airbyte.io/company-handbook/company-handbook/business-model). 

### **What kind of notifications do I get?**

For the moment, the UI will only display one kind of notification: when a sync fails, we will display the failure at the source level in the list of sources and in the source detail page along with the logs. 

However, there are other types of notifications we’re thinking about: 

* When a connector that you use is no longer up to date 
* An email notification when one of your connections fails

### **Where's the T in Airbyte’s ETL tool?**

Airbyte is actually an ELT tool, and you have the freedom to use it as an EL-only tool. The transformation part is done by default, but it is optional. You can choose to receive the data in raw \(JSON file for instance\) in your destination. 

We do provide normalization \(if option is still on\) so that data analysts / scientists / any users of the data can use it without much effort. 

### **How does Airbyte determine when to replicate my data?**

You can specify the replication frequency on a connector basis, which determines how often Airbyte will attempt to extract data from the data source. You can do that in the Settings tab of the source. 

### I see you support a lot of integrations – what about integrations Airbyte doesn’t support yet?

You can either:

* **Submit an** [**integration request**](https://github.com/airbytehq/airbyte/issues/new?assignees=&labels=area%2Fintegration%2C+new-integration&template=new-integration-request.md&title=) on our Github project, and be notified once we or the community build a connector for it.
* **Build a connector yourself** by forking our [GitHub project](https://github.com/airbytehq/airbyte) and submitting a pull request. Don’t hesitate to ping the team on [Slack](http://slack.airbyte.io), so we can check your PR as soon as possible. But you do NOT need to wait for the PR to be approved to benefit from your own fix. Put your connector in a new folder, and add your connector directly through our UI by clicking on + New connector in the Admin section. This way, you will be able to use your connector as a separate one from the connector available to the community. 

\*\*\*\*

