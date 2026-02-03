import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"
import { cn} from "../../../utils/cs";

const badgeVariants = cva(
    "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
    {
        variants: {
            variant: {
                default:
                    "bg-primary text-white hover:bg-primary/80 dark:bg-primary-light dark:text-gray-900",
                secondary:
                    "bg-secondary text-white hover:bg-secondary/80 dark:bg-secondary-light dark:text-gray-900",
                destructive:
                    "bg-red-500 text-white hover:bg-red-600 dark:bg-red-600 dark:hover:bg-red-700",
                outline:
                    "border border-gray-300 bg-transparent hover:bg-gray-100 dark:border-gray-600 dark:hover:bg-gray-800 dark:text-gray-300",
                success:
                    "bg-status-success bg-opacity-20 text-status-success border border-status-success border-opacity-20 dark:bg-opacity-30",
                warning:
                    "bg-status-warning bg-opacity-20 text-status-warning border border-status-warning border-opacity-20 dark:bg-opacity-30",
                error:
                    "bg-status-error bg-opacity-20 text-status-error border border-status-error border-opacity-20 dark:bg-opacity-30",
                info:
                    "bg-status-info bg-opacity-20 text-status-info border border-status-info border-opacity-20 dark:bg-opacity-30",
            },
            size: {
                default: "h-6 px-2.5 py-0.5 text-xs",
                sm: "h-5 px-2 py-0 text-xs",
                lg: "h-7 px-3 py-1 text-sm",
            },
        },
        defaultVariants: {
            variant: "default",
            size: "default",
        },
    }
)

export interface BadgeProps
    extends React.HTMLAttributes<HTMLDivElement>,
        VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, size, ...props }: BadgeProps) {
    return (
        <div className={cn(badgeVariants({ variant, size }), className)} {...props} />
    )
}

export { Badge, badgeVariants }