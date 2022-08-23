# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), with additional info about the chronology things are added/fixed/changed and - where possible - links to the PRs involved.


### Changes
[XM2C-30] 
* added checksum of configmap and secrets for deployment for **Automatically Roll Deployments**
* changed mq default strategy from RollingUpdate to Recreate because of conflict in updating 
