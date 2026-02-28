# Core building blocks of Terraform:

---

# Providers

### What is a Provider?

A provider is a plugin that allows Terraform to talk to a platform.

Examples:

* Azure → `azurerm`
* AWS → `aws`
* Kubernetes → `kubernetes`
* GitHub → `github`

Without a provider, Terraform can’t create anything.

Example:

```hcl
provider "azurerm" {
  features {}
}
```

What happens internally:

1. `terraform init` downloads the provider binary.
2. Provider authenticates.
3. Provider calls cloud APIs.
4. Provider translates Terraform config → API calls.

So provider = API bridge.

Think:

Terraform = Brain
Provider = Hands that call APIs

---

# State

This is where most confusion starts.

Terraform is declarative.
You define desired state.

But how does Terraform know what already exists?

→ It stores information in a file called:

```
terraform.tfstate
```

State contains:

* Resource IDs
* Current attributes
* Dependency mapping

Why state exists:

Because Terraform must compare:

Current real infrastructure
vs
Your configuration files

Then it computes a diff.

That’s what `terraform plan` shows.

If state is lost:

* Terraform thinks nothing exists
* It will try to recreate everything

So state is Terraform’s memory.

---

# Backend

By default:

State is stored locally.

That’s fine for personal projects.

But in real DevOps teams?

Local state = disaster.

Why?

* No collaboration
* No locking
* Risk of corruption
* No versioning

So we configure a backend.

Backend = where state is stored.

Example Azure backend:

```hcl
terraform {
  backend "azurerm" {
    resource_group_name  = "tfstate-rg"
    storage_account_name = "tfstateaccount"
    container_name       = "tfstate"
    key                  = "dev.terraform.tfstate"
  }
}
```

Now state is:

* Stored in Azure Blob Storage
* Locked during apply
* Shared across team
* Safer

Backend manages state storage.

State = data
Backend = storage location

---

# Variables

Variables make Terraform reusable.

Without variables:

```hcl
location = "South India"
```

Hardcoded.

With variables:

```hcl
variable "location" {
  default = "South India"
}
```

Use it:

```hcl
location = var.location
```

Now you can:

* Change environment
* Change VM size
* Parameterize deployments

Variables help with:

* Environment separation
* Reusability
* Cleaner code

Think of them like function parameters in programming.

---

# Modules

Modules are reusable Terraform components.

If you repeat:

* VNet creation
* VM creation
* AKS setup

Across projects, that’s messy.

Instead, you create a module.

Example:

```
modules/network
modules/vm
```

Then call it:

```hcl
module "network" {
  source   = "../../modules/network"
  location = var.location
}
```

Modules allow:

* Code reuse
* Clean separation
* Standardization
* Enterprise patterns

Root module = main project
Child modules = reusable components

Think:

Modules are like functions or classes in programming.

---

# Workspaces

Workspaces allow multiple state files in the same configuration.

Default workspace:

```
default
```

Create new workspace:

```bash
terraform workspace new dev
terraform workspace new prod
```

Each workspace:

* Has its own state
* Uses same code
* Different infrastructure

Useful for:

* Dev
* QA
* Prod

But important:

Workspaces are not a full environment strategy.

In real projects, we often prefer:

* Separate folders per environment
* Separate backend keys

Workspaces are good for:

* Simple separation
* Temporary environments

Not ideal for complex enterprise setups.

---

# How Everything Connects

When you run:

```bash
terraform init
terraform plan
terraform apply
```

Terraform does this:

1. Load configuration
2. Load variables
3. Load state from backend
4. Initialize provider
5. Compare desired vs current
6. Build dependency graph
7. Apply changes via provider
8. Update state

Every concept plays a role:

Provider → API communication
State → Memory
Backend → Where memory is stored
Variables → Input parameters
Modules → Reusable blocks
Workspaces → Multiple state environments



